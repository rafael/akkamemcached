package org.memcached
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.memcached.tcp.TcpServer

object Main {
  def main(args: Array[String]) {
    implicit val actorSystem       = ActorSystem("memcached-actor-system")
    implicit val actorMaterializer = ActorMaterializer()
    implicit val executor          = actorSystem.dispatcher
    sys.addShutdownHook(actorSystem.terminate())
    TcpServer.actorOf()
  }
}
