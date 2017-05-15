package org.memcached.tcp

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props, Terminated}
import akka.util.ByteString
import org.memcached.types.VersionCmd
import org.memcached.types.protocol._
import org.memcached.utils.BinaryProtocolHelpers._
import org.memcached.utils.ServerConfig

import scala.util.{Failure, Success, Try}

/**
  * Created by rafael on 5/9/17.
  */

class CommandHandler(cache: ActorRef) extends Actor with ActorLogging {

  import akka.io.Tcp._

  // Watch the sender (i.e connection)
  context watch sender

  /**
    * The responsibility of this actor is to receive messages from the connection actor. It validated the data that came
    * in the wire. Parses the command and if it's successful, delegates the responsibility of fulfilling the command to
    * the cache actor.
    * These actor could receive the following messages of messages that can be received:
    *  Received(data): Raw ByteString sent by the client
    *  PeerClosed: The client closed the connection
    *  Terminated: We are watching the connection, so if for some reason the connection actor dies, we make sure to clean
    *  up the state.
    */

  def receive = {
    // This is the data coming from the TCP socket. Trying to parse as a binary Memcached
    // command
    case Received(data) =>
      log.debug(s"Received the following data: ${data.toString()}")
      val requestHeaderTry = RequestHeader(data.slice(0,24))
      validateRequestHeaderAndProcessCmd(data, requestHeaderTry)
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

  /**
    * This is a convenience method to do some validations in the request header. If it succeeds,
    * allow the request to come in and process it.
    * @param data the original raw byte array
    * @param requestHeaderTry a parsed RequestHeader
    */
  private def validateRequestHeaderAndProcessCmd(data: ByteString, requestHeaderTry: Try[RequestHeader]) = {
    requestHeaderTry match {
      // Key length exceeded rejecting request,
      case Success(requestHeader) if requestHeader.keyLength > ServerConfig.keyMaxLength =>
        log.info("Invalid key size")
        sender() ! Write(buildErrorResponse(InvalidArguments, requestHeader.opcode))
      // Value length exceeded rejecting request,
      case Success(requestHeader) if requestHeader.opcode == Set && requestHeader.totalBodyLength - requestHeader.extrasLength - requestHeader.keyLength > ServerConfig.itemMaxSize =>
        log.info("Invalid item size")
        sender() ! Write(buildErrorResponse(ValueTooLarge, requestHeader.opcode))
      case Success(requestHeader) =>
        parseCommand(data, requestHeader)
      case Failure(error) =>
        log.error(error, "Un-parsable header received")
        sender() ! Write(buildErrorResponse(UnknownCommand, Get))
    }
  }

  /**
    * Once the request have been validated and the headers parsed, this method will try to map it to a supported
    * command by the server and delegate to the cache to fulfill the request.
    * @param data Raw byte array received from the connection
    * @param requestHeader  A valid parsed requestHeader
    */
  private def parseCommand(data: ByteString, requestHeader: RequestHeader) = {
    val cmdTry = parseServerCmd(requestHeader, data)
    cmdTry match {
      case Success(cmd) if cmd == VersionCmd =>
        log.debug(s"VersionCmd processed - Sending response: $versionResponse")
        sender() ! Write(versionResponse)
      case Success(cmd) =>
        log.debug(s"Command successfully parsed: $cmd")
        val originalSender = sender()
        // The message here comes from the connection actor, by using the tell method, we set who the cache should
        // respond (connection) when it processes the request.
        cache.tell(cmd, originalSender)
      case Failure(error) =>
        log.error(error, "Un-parsable command received")
        sender() ! Write(buildErrorResponse(UnknownCommand, Get))
    }
  }
}

object CommandHandler {
  private [tcp] val count = new AtomicInteger(0)

  private [memcached] def actorOf(cache: ActorRef)(implicit actorRefFactory: ActorRefFactory):ActorRef =
    actorRefFactory.actorOf(props(cache), name)

  private[memcached] def props(cache: ActorRef): Props = Props(classOf[CommandHandler], cache)

  private [memcached] def name = s"command-handler-actor-${count.incrementAndGet}"
}
