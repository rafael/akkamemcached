package org.memcached.types.protocol

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
  def apply(magicValue: Int): Option[MagicValue] = {
    if (magicValue == RequestPacketMagic.code)
      Some(RequestPacketMagic)
    else if (magicValue == ResponsePacketMagic.code)
      Some(ResponsePacketMagic)
    else
      None
  }
}

