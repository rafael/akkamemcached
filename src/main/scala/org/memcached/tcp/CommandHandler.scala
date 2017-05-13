package org.memcached.tcp

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props, Terminated}
import akka.util.ByteString
import org.memcached.types.VersionCmd
import org.memcached.types.protocol.{Get, RequestHeader, UnknownCommand}
import org.memcached.utils.BinaryProtocolHelpers._

import scala.util.{Failure, Success}

/**
  * Created by rafael on 5/9/17.
  */

class CommandHandler(connection: ActorRef, cache: ActorRef) extends Actor with ActorLogging {

  import akka.io.Tcp._

  context watch connection

  def receive = {
    case Received(data) =>
      log.debug(s"Received the following data: ${data.toString()}")
      val requestHeader = RequestHeader(data.slice(0,24))
      val cmdTry = requestHeader.flatMap(parseServerCmd(_, data))
      cmdTry match {
        case Success(cmd) if cmd == VersionCmd =>
          log.debug(s"VersionCmd processed - Sending response: $versionResponse")
          sender() ! Write(versionResponse)
        case Success(cmd) =>
          log.debug(s"Command successfully parsed: $cmd")
          val originalSender = sender()
          cache.tell(cmd, originalSender)
        case Failure(error) =>
          log.error(error, "Un-parsable command received")
          sender() ! Write(buildErrorResponse(UnknownCommand, Get))
      }
    case PeerClosed =>
      log.debug("Connection closed")
      context stop self
    case Terminated(watched) =>
      log.error(s"Connection $watched was terminated")
      context stop self
    case unhandled =>
      log.error(s"Unhandled message received: $unhandled")
      context stop self
  }
}

object CommandHandler {
  private [tcp] val count = new AtomicInteger(0)

  private [memcached] def actorOf(connection: ActorRef, cache: ActorRef)(implicit actorRefFactory: ActorRefFactory):ActorRef =
    actorRefFactory.actorOf(props(connection, cache), name)

  private[memcached] def props(connection: ActorRef, cache: ActorRef): Props = Props(classOf[CommandHandler], connection, cache)

  private [memcached] def name = s"command-handler-actor-${count.incrementAndGet}"
}
