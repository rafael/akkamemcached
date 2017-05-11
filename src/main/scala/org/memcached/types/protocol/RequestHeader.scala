package org.memcached.types.protocol

import akka.util.ByteString

/**
  * Created by rafael on 5/10/17.
  */
/**
  * Data Type to represent a request header in Memcached Binary protocol
  * <p>
  * @param  opcode Command code.
  * @param  keyLength Length in bytes of the text key that follows the command extras.
  * @param  extrasLength  Length in bytes of the command extras.
  * @param  reserved  Really reserved for future use (up for grabs).
  * @param  totalBodyLength Length in bytes of extra + key + value.
  * @param  opaque Will be copied back to you in the response.
  * @param  cas Data version check.
  */
case class RequestHeader(
                          opcode: Opcode,
                          keyLength: Int,
                          extrasLength: Int,
                          reserved: Int,
                          totalBodyLength: Int,
                          opaque: Long,
                          cas: Long
                        ) {
  // Per protocol version definition for RequestPacket header
  val magic =  RequestPacketMagic
}

object RequestHeader {
  import org.memcached.utils.BinaryProtocolHelpers._
  def apply(data: ByteString):Option[RequestHeader] = {
    val magicOpt: Option[MagicValue] = MagicValue(readLong(data.slice(0,1)).toInt)
    val opcodeOpt: Option[Opcode] = Opcode(readLong(data.slice(1,2)).toInt)
    val keyLength: Int = readLong(data.slice(2,4)).toInt
    val extrasLength: Int = readLong(data.slice(4,5)).toInt
    val reserved: Int = readLong(data.slice(6,8)).toInt
    val totalBodyLength: Int = readLong(data.slice(8,12)).toInt
    val opaque: Long = readLong(data.slice(12,16))
    val cas: Long = readLong(data.slice(12,16))
    for {
      magic <- magicOpt
      opcode <- opcodeOpt
      if magic == RequestPacketMagic
    } yield
      RequestHeader(
        opcode = opcode,
        keyLength =  keyLength,
        extrasLength = extrasLength,
        reserved = reserved,
        totalBodyLength = totalBodyLength,
        opaque = opaque,
        cas = cas
      )
  }
}

