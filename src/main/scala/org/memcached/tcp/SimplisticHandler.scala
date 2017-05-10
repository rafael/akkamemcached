package org.memcached.tcp

import akka.actor.{Actor, ActorLogging}

/**
  * Created by rafael on 5/9/17.
  */

class SimplisticHandler extends Actor with ActorLogging {
  import akka.io.Tcp._
  def receive = {
    case Received(data) => sender() ! Write(data)
    case PeerClosed     =>
      log.debug("Connection closed")
      context stop self
    case unhandled =>
      log.error(s"Unhandled message received: $unhandled")
  }
}
