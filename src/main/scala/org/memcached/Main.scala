package org.memcached
import akka.actor.ActorSystem
import org.memcached.cache.Bucket
import org.memcached.tcp.TcpServer

object Main {
  def main(args: Array[String]) {
    implicit val actorSystem       = ActorSystem("memcached-actor-system")
    implicit val executor          = actorSystem.dispatcher
    sys.addShutdownHook(actorSystem.terminate())
    lazy val cache = Bucket.actorOf(1024*1024)
    TcpServer.actorOf(cache)
  }
}
