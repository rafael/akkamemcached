package org.memcached.utils

import com.typesafe.config.ConfigFactory

/**
  * Created by rafael on 5/14/17.
  */
object ServerConfig {
  private val conf = ConfigFactory.load()
  private val prefix = "memcached"
  val itemMaxSize:Long = conf.getLong(s"$prefix.item-max-size")
  val port:Int = conf.getInt(s"$prefix.port")
  val cacheSize:Long = conf.getLong(s"$prefix.cache-size")
  val keyMaxLength:Long = conf.getLong(s"$prefix.key-max-length")
}
