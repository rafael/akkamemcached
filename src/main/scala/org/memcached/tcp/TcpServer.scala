package org.memcached.tcp

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props}
import akka.io.{IO, Tcp}


class TcpServer extends Actor with ActorLogging {

  import akka.io.Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 11211))

  def receive = {
    case Bound(localAddress) =>
      log.info(s"Successfully bound to ${localAddress.getAddress}:${localAddress.getPort}")

    case CommandFailed(_: Bind) =>
      log.error("Fatal error, couldn't bind to port")
      context stop self

    case Connected(remote, _) =>
      log.debug(s"New connection accepted for ${remote.getAddress}:${remote.getPort}")
      val connection = sender()
      val handler = CommandHandler.actorOf(connection)(context)
      connection ! Register(handler)
    case unhandled =>
      log.error(s"Unhandled message received: $unhandled")
  }
}

object TcpServer {
  private [memcached] def actorOf()(implicit actorRefFactory: ActorRefFactory):ActorRef =
    actorRefFactory.actorOf(props(), name)

  private[memcached] def props(): Props = Props(classOf[TcpServer])

  private [memcached] def name = "tcp-server-actor"
}
