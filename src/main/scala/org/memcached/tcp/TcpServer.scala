package org.memcached.tcp

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props}
import akka.io.{IO, Tcp}
import org.memcached.utils.ServerConfig

class TcpServer(cache: ActorRef) extends Actor with ActorLogging {

  import akka.io.Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("0.0.0.0", ServerConfig.port))

  /**
    * This actor defines the tcp server. It starts by binding itself to 0.0.0.0 in the port provided by configuration
    * It could receive the following messages:
    * Bound(localAddress): Message received as response from the IO actor to the bind message. It tells us the we are
    * ready to accept connections
    * CommandFailed: We couldn't start the server
    * Connected: There is a new established connection with a client. Create a new CommandHandler actor that will listen to
    * messages from this client.
    */
  def receive = {
    case Bound(localAddress) =>
      log.info(s"Successfully bound to ${localAddress.getAddress}:${localAddress.getPort}")
    case CommandFailed(_: Bind) =>
      log.error("Fatal error, couldn't bind to port")
      context stop self
    case Connected(remote, _) =>
      log.debug(s"New connection accepted for ${remote.getAddress}:${remote.getPort}")
      val connection = sender()
      val handler = CommandHandler.actorOf(cache)(context)
      connection ! Register(handler)
    case unhandled =>
      log.error(s"Unhandled message received: $unhandled")
  }
}

object TcpServer {
  private [memcached] def actorOf(cache: ActorRef)(implicit actorRefFactory: ActorRefFactory):ActorRef =
    actorRefFactory.actorOf(props(cache), name)

  private[memcached] def props(cache: ActorRef): Props = Props(classOf[TcpServer], cache)

  private [memcached] def name = "tcp-server-actor"
}
