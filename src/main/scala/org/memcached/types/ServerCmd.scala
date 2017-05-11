package org.memcached.types

import akka.util.ByteString

/**
  * Created by rafael on 5/9/17.
  */

trait ServerCmd


case class SetCmd(key: String, payload: ByteString, expiresAt: Option[Long] = None) extends  ServerCmd
case class GetCmd(key: String) extends  ServerCmd
case class CasCmd(key: String, payload: ByteString, checksum: String) extends  ServerCmd
case class DeleteCmd(key: String) extends  ServerCmd

