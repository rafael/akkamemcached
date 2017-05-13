package org.memcached.cache

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props}
import akka.util.ByteString
import org.memcached.types.{GetCmd, SetCmd}
import org.memcached.types.protocol._
import org.memcached.utils.BinaryProtocolHelpers._


case class CacheValue(value: ByteString, cas: Long, flags: ByteString)

class Bucket(maxSizeInBytes: Long) extends Actor with ActorLogging {

  import akka.io.Tcp._

  var cache: Map[ByteString, CacheValue] = Map()

  def receive = {
    case SetCmd(key, value, cas, flags)  =>
      cache += (key -> CacheValue(value, cas, flags))
      log.debug("SetCmd processed")
      sender ! Write(buildSetResponse(2000))
    case GetCmd(key)  =>
      cache.get(key) match {
        case Some(cacheValue)  =>
          log.debug(s"Get Cmd processed - Found key: $key")
          val response = buildGetRequestResponse(0, cacheValue.value, cacheValue.cas, cacheValue.flags)
          log.debug(s"Sending response: $response")
          sender ! Write(response)
        case None =>
          log.debug(s"GetCmd processed - KeyNotFound: $key")
          sender ! Write(buildErrorResponse(KeyNotFound, Get))
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
