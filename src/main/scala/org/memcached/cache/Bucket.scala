package org.memcached.cache

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props}
import akka.util.ByteString
import org.memcached.types.{Lru, SizeInBytes}
import org.memcached.types.{DeleteCmd, GetCmd, SetCmd}
import org.memcached.types.protocol._
import org.memcached.utils.BinaryProtocolHelpers._
import org.memcached.utils.ServerConfig

case class CacheValue(value: ByteString, cas: Long, flags: ByteString)

class Bucket(maxSizeInBytes: Long) extends Actor with ActorLogging {

  import akka.io.Tcp._

  implicit object Sizer extends SizeInBytes[CacheValue] {
    def size(x: CacheValue ): Int = x.value.size
  }

  val cache: Lru[ByteString, CacheValue] = Lru[ByteString, CacheValue](
    maxCacheSizeBytes =  maxSizeInBytes,
    itemMaxSizeInBytes = ServerConfig.itemMaxSize)

  def receive = {
    // Not a CAS operation
    case SetCmd(key, value, cas, flags) if cas == 0 =>
      cache.set(key, CacheValue(value, 1, flags))
      log.debug("SetCmd processed")
      sender ! Write(buildResponseNoBody(Get, cas = 1))
    // Client trying to perform CAS operation, checking if
    // if it can succeed or not
    case SetCmd(key, value, cas, flags) if cas != 0 =>
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
    case GetCmd(key)  =>
      cache.get(key) match {
        case Some(cacheValue)  =>
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
    case DeleteCmd(key) =>
      if (cache.delete(key)) {
        log.debug(s"DeleteCmd processed - KeyDeleted: $key")
        sender ! Write(buildResponseNoBody(Delete, cas = 0))
      } else {
        log.debug(s"DeleteCmd processed - KeyNotFound: $key")
        sender ! Write(buildErrorResponse(KeyNotFound, Delete))
      }
    case unhandled =>
      log.error(s"Bucket received unhandled message received: $unhandled")
  }
}

object Bucket {
  private [cache] val count = new AtomicInteger(0)

  private [memcached] def actorOf(maxSizeBytes: Long)(implicit actorRefFactory: ActorRefFactory):ActorRef =
    actorRefFactory.actorOf(props(maxSizeBytes), name)

  private[memcached] def props(maxSizeBytes: Long): Props = Props(classOf[Bucket], maxSizeBytes)

  private [memcached] def name = s"cache-bucket-actor-${count.incrementAndGet}"
}
