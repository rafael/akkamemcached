package org.memcached
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.memcached.cache.Bucket
import org.memcached.tcp.TcpServer
import org.memcached.utils.ServerConfig

object Main {
  def main(args: Array[String]) {
    implicit val actorSystem       = ActorSystem("memcached-actor-system")
    implicit val executor          = actorSystem.dispatcher
    sys.addShutdownHook(actorSystem.terminate())
    lazy val cache = Bucket.actorOf(ServerConfig.cacheSize)
    TcpServer.actorOf(cache)
  }
}
