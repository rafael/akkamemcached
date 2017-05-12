package org.memcached.tcp

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props}
import akka.util.ByteString
import org.memcached.types.VersionCmd
import org.memcached.types.protocol.RequestHeader
import org.memcached.utils.BinaryProtocolHelpers._

import scala.util.{Failure, Success}

/**
  * Created by rafael on 5/9/17.
  */

class CommandHandler(connection: ActorRef) extends Actor with ActorLogging {

  import akka.io.Tcp._

  context watch connection

  def receive = {
    case Received(data) =>
      log.debug(s"Received the following data: ${data.toString()}")
      val requestHeader = RequestHeader(data.slice(0,24))
      val cmdTry = requestHeader.flatMap(parseServerCmd(_, data))
      cmdTry match {
        case Success(cmd) if cmd == VersionCmd => {
          log.debug(s"Sent version response: $versionResponse")
          sender() ! Write(versionResponse)
        }
        case Success(cmd) => {
          log.debug(s"Yei! Command successfully parsed: $cmd")
          val response = buildGetRequestResponse(0, ByteString("World".getBytes()))
          log.debug(s"Yei! Sending response: $response")
          sender() ! Write(response)
        }
        case Failure(error) => {
          log.error(error, "Couldn't understand this command")
          sender() ! Write(data)
        }
      }
    case PeerClosed =>
      log.debug("Connection closed")
      context stop self
    case unhandled =>
      log.error(s"Unhandled message received: $unhandled")
  }
}

object CommandHandler {
  private [tcp] val count = new AtomicInteger(0)

  private [memcached] def actorOf(connection: ActorRef)(implicit actorRefFactory: ActorRefFactory):ActorRef =
    actorRefFactory.actorOf(props(connection), name)

  private[memcached] def props(connection: ActorRef): Props = Props(classOf[CommandHandler], connection)

  private [memcached] def name = s"command-handler-actor-${count.incrementAndGet}"
}
