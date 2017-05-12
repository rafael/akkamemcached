package org.memcached.cache

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props}
import akka.util.ByteString
import org.memcached.types.{GetCmd, SetCmd}
import org.memcached.types.protocol.RequestHeader
import org.memcached.utils.BinaryProtocolHelpers.{buildGetRequestResponse, parseServerCmd, versionResponse}


class Bucket(client: ActorRef) extends Actor with ActorLogging {

  import akka.io.Tcp._

  var cache: Map[ByteString, ByteString] = Map()

  def receive = {
    case SetCmd(key, value, cas)  =>
      cache += (key -> value)
      log.info("Test")
      client ! buildGetRequestResponse(0, value)
    case GetCmd(key)  =>
      cache.get(key) match {
        case Some(value)  =>
          log.info("Test")
          client ! buildGetRequestResponse(0, value)
        case None =>
          log.info("Value not found")
      }
    case unhandled =>
      log.error(s"Unhandled message received: $unhandled")
  }
}

object Bucket {
  private [tcp] val count = new AtomicInteger(0)

  private [memcached] def actorOf(connection: ActorRef)(implicit actorRefFactory: ActorRefFactory):ActorRef =
    actorRefFactory.actorOf(props(connection), name)

  private[memcached] def props(connection: ActorRef): Props = Props(classOf[Bucket], connection)

  private [memcached] def name = s"cache-bucket-actor-${count.incrementAndGet}"
}
