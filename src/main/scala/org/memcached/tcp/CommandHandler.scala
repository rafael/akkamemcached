package org.memcached.tcp

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props}

/**
  * Created by rafael on 5/9/17.
  */

class CommandHandler(connection: ActorRef) extends Actor with ActorLogging {

  import akka.io.Tcp._

  context watch connection

  def receive = {
    case Received(data) =>
      log.debug(s"Received the following data: ${data.toString()}")
      sender() ! Write(data)
    case PeerClosed     =>
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
