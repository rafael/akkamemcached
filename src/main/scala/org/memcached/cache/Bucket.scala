package org.memcached.cache

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props}
import akka.util.ByteString
import org.memcached.types.{Lru, SizeInBytes}
import org.memcached.types.{DeleteCmd, GetCmd, SetCmd}
import org.memcached.types.protocol._
import org.memcached.utils.BinaryProtocolHelpers._
import org.memcached.utils.ServerConfig

/**
  * This case class represents a value that is stored in the cache
  * @param value ByteString representation of the value stored
  * @param cas The current cas value for this object.
  * @param flags Flags provided by the client when setting the object.
  */
case class CacheValue(value: ByteString, cas: Long, flags: ByteString)

class Bucket(maxSizeInBytes: Long) extends Actor with ActorLogging {

  import akka.io.Tcp._

  /**
    * This implicit object allows us to calculate the size of a Cache item.
    * Right now, the size is the defined as the size of the value.
    * If we would to consider the keys as well, we just need to modify
    * this method.
    */
  implicit object Sizer extends SizeInBytes[CacheValue] {
    def size(x: CacheValue ): Int = x.value.size
  }

  // This the holder of the state of the cache. Given the thread safety provided
  // by the actor model, we know operations on this case will be atomic.
  val cache: Lru[ByteString, CacheValue] = Lru[ByteString, CacheValue](
    maxCacheSizeBytes =  maxSizeInBytes,
    itemMaxSizeInBytes = ServerConfig.itemMaxSize)

  /**
    * Main receive method for this actor. It understands the commands supported by the cache.
    * This actor processes the command and then replies back to the connection actor with data it should send to the
    * client.
    *
    */
  def receive = {
    // if cas is equal to 0, it means that the client doesn't care if the value has changed since it last saw it
    case SetCmd(key, value, cas, flags) if cas == 0 =>
      setCmd(key, value, flags)
    // Client trying to perform CAS operation, this operation will only succeed if the cas value stored in the cache
    // is the same as the one provided by the client.
    case SetCmd(key, value, cas, flags) if cas != 0 =>
      setCmdWithCas(key, value, cas, flags)
    case GetCmd(key)  =>
      getCmd(key)
    case DeleteCmd(key) =>
      deleteCmd(key)
    case unhandled =>
      log.error(s"Bucket received unhandled message received: $unhandled")
  }

  private def deleteCmd(key: ByteString) = {
    if (cache.delete(key)) {
      log.debug(s"DeleteCmd processed - KeyDeleted: $key")
      sender ! Write(buildResponseNoBody(Delete, cas = 0))
    } else {
      log.debug(s"DeleteCmd processed - KeyNotFound: $key")
      sender ! Write(buildErrorResponse(KeyNotFound, Delete))
    }
  }

  private def getCmd(key: ByteString) = {
    cache.get(key) match {
      case Some(cacheValue) =>
        log.debug(s"Get Cmd processed - Found key: $key")
        val response = buildGetRequestResponse(
          opaque = 0,
          cacheValue.value,
          cacheValue.cas,
          cacheValue.flags
        )
        log.debug(s"Sending response: $response")
        sender ! Write(response)
      case None =>
        log.debug(s"GetCmd processed - KeyNotFound: $key")
        sender ! Write(buildErrorResponse(KeyNotFound, Get))
    }
  }

  /**
    * This method checks if the key exists in the cache. If it's not there, it fails.
    * The cas is stored as Long value that gets incremented every time the operation succeeds.
    * @param key to set in the cache
    * @param value new value for the key
    * @param cas provided by the client
    * @param flags flags to store along the value
    */
  private def setCmdWithCas(key: ByteString, value: ByteString, cas: Long, flags: ByteString) = {
    cache.get(key) match {
      case Some(cacheValue) if cacheValue.cas == cas =>
        log.debug("SetCmd processed with cas. CAS matched. Write successful")
        cache.set(key, CacheValue(value, cas + 1, flags))
        sender ! Write(buildResponseNoBody(Get, cas + 1))
      case Some(_) =>
        log.debug("SetCmd processed with cas. CAS didn't match. Failing")
        sender ! Write(buildErrorResponse(ItemNotStored, Set))
      case None =>
        log.debug(s"GetCmd processed - KeyNotFound: $key")
        sender ! Write(buildErrorResponse(KeyNotFound, Set))
    }
  }

  private def setCmd(key: ByteString, value: ByteString, flags: ByteString) = {
    cache.set(key, CacheValue(value, 1, flags))
    log.debug("SetCmd processed")
    sender ! Write(buildResponseNoBody(Get, cas = 1))
  }
}

object Bucket {
  private [cache] val count = new AtomicInteger(0)

  private [memcached] def actorOf(maxSizeBytes: Long)(implicit actorRefFactory: ActorRefFactory):ActorRef =
    actorRefFactory.actorOf(props(maxSizeBytes), name)

  private[memcached] def props(maxSizeBytes: Long): Props = Props(classOf[Bucket], maxSizeBytes)

  private [memcached] def name = s"cache-bucket-actor-${count.incrementAndGet}"
}
