package org.memcached.types

import akka.util.ByteString

/**
  * Created by rafael on 5/9/17.
  */

trait ServerCmd

// Types for the supported operations by the server.
case class SetCmd(key: ByteString,
                  payload: ByteString,
                  cas: Long,
                  flags: ByteString) extends  ServerCmd
case class GetCmd(key: ByteString) extends  ServerCmd
case class DeleteCmd(key: ByteString) extends  ServerCmd
case object VersionCmd extends ServerCmd

