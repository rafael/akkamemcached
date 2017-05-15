package org.memcached
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.memcached.cache.Bucket
import org.memcached.tcp.TcpServer
import org.memcached.utils.ServerConfig

object Main {
  def main(args: Array[String]) {
    implicit val actorSystem = ActorSystem("memcached-actor-system")
    sys.addShutdownHook(actorSystem.terminate())
    // Creates the cache actor
    lazy val cache = Bucket.actorOf(ServerConfig.cacheSize)
    // Starts TCP server
    TcpServer.actorOf(cache)
  }
}
