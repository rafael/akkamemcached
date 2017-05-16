package org.memcached.types.protocol

import akka.util.ByteString
import org.memcached.utils.BinaryProtocolHelpers._

import scala.util.Try

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
                          totalBodyLength: Long,
                          opaque: Long,
                          cas: Long
                        ) {
  // Per protocol version definition for RequestPacket header
  val magic =  RequestPacketMagic
  val dataType = 0

  /**
    *
    * Byte/     0       |       1       |       2       |       3       |
    *    /              |               |               |               |
    *   |0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|
    *   +---------------+---------------+---------------+---------------+
    *  0| Magic         | Opcode        | Key length                    |
    *   +---------------+---------------+---------------+---------------+
    *  4| Extras length | Data type     | Reserved                      |
    *   +---------------+---------------+---------------+---------------+
    *  8| Total body length                                             |
    *   +---------------+---------------+---------------+---------------+
    * 12| Opaque                                                        |
    *   +---------------+---------------+---------------+---------------+
    * 16| CAS                                                           |
    *   |                                                               |
    *   +---------------+---------------+---------------+---------------+
    *
    *  @return ByteString representation of the RequestHeader packet
   */
  def toByteString:ByteString = {
    longToByteString(magic.code,     1) ++
    longToByteString(opcode.code,    1) ++
    longToByteString(keyLength,      2) ++
    longToByteString(extrasLength,   1) ++
    longToByteString(dataType,       1) ++
    longToByteString(reserved,       2) ++
    longToByteString(totalBodyLength,4) ++
    longToByteString(opaque,         4) ++
    longToByteString(cas,            8).reverse
  }
}

object RequestHeader {
  /**
    * Constructs a request header from a ByteString
    * @param data ByteString that contains the request
    * @return
    */
  def apply(data: ByteString):Try[RequestHeader] = {
    val magicTry: Try[MagicValue] = MagicValue(byteStringToLong(data.slice(0,1)).toInt)
    val opcodeTry: Try[Opcode] = Opcode(byteStringToLong(data.slice(1,2)).toInt)
    val keyLength: Int = byteStringToLong(data.slice(2,4)).toInt
    val extrasLength: Int = byteStringToLong(data.slice(4,5)).toInt
    val reserved: Int = byteStringToLong(data.slice(6,8)).toInt
    val totalBodyLength: Long = byteStringToLong(data.slice(8,12))
    val opaque: Long = byteStringToLong(data.slice(12,16))
    // See comment in ResponseHeader for explanation why, I'm reversing this.
    val cas: Long = byteStringToLong(data.slice(16,24).reverse)
    for {
      magic <- magicTry
      opcode <- opcodeTry
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

