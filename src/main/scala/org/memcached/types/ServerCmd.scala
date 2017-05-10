package org.memcached.types

import akka.util.ByteString

/**
  * Created by rafael on 5/9/17.
  */

trait ServerCmd


case class Set(key: String, payload: ByteString, expiresAt: Option[Long] = None) extends  ServerCmd
case class Get(key: String) extends  ServerCmd
case class Cas(key: String, payload: ByteString, checksum: String) extends  ServerCmd
case class Delete(key: String) extends  ServerCmd

object test {
  val x = ByteString(List(-128, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 97).map(_.toByte).toArray)
}
