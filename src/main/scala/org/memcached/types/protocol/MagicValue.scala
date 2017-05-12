package org.memcached.types.protocol

import scala.util.{Success, Try, Failure}

/**
  * Created by rafael on 5/9/17.
  */

trait MagicValue {
  def code: Int
}

case object ResponsePacketMagic extends  MagicValue {
  override  val code = 0x81
}

case object RequestPacketMagic extends  MagicValue {
  override  val code = 0x80
}

object MagicValue {
  def apply(magicValue: Int): Try[MagicValue] = {
    if (magicValue == RequestPacketMagic.code)
      Success(RequestPacketMagic)
    else if (magicValue == ResponsePacketMagic.code)
      Success(ResponsePacketMagic)
    else
      Failure(new RuntimeException(s"Invalid magic value:  $magicValue"))
  }
}

