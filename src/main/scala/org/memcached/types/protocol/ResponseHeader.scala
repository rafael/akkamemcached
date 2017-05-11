package org.memcached.types.protocol

import akka.util.ByteString
import org.memcached.utils.BinaryProtocolHelpers._

/**
  * Created by rafael on 5/10/17.
  */
/**
  * Data Type to represent a response header in Memcached Binary protocol
  * <p>
  * @param  opcode Command code.
  * @param  keyLength Length in bytes of the text key that follows the command extras.
  * @param  extrasLength  Length in bytes of the command extras.
  * @param  status  Status of the response (non-zero on error).
  * @param  totalBodyLength Length in bytes of extra + key + value.
  * @param  opaque Will be copied back to you in the response.
  * @param  cas Data version check.
  */
case class ResponseHeader(
                           opcode: Opcode,
                           keyLength: Int,
                           extrasLength: Int,
                           status: ResponseStatus,
                           totalBodyLength: Long,
                           opaque: Long,
                           cas: Long
                         ) {
  // Per protocol version definition for ResponsePacket header
  val magic = ResponsePacketMagic
  // Per protocol this is not being used at the moment:
  // Reserved for future use (Sean is using this soon). (WHO IS SEAN)???
  val dataType = 0

  /**
    *
    *    Byte/     0       |       1       |       2       |       3       |
    *       /              |               |               |               |
    *      |0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|
    *      +---------------+---------------+---------------+---------------+
    *     0| Magic         | Opcode        | Key Length                    |
    *      +---------------+---------------+---------------+---------------+
    *     4| Extras length | Data type     | Status                        |
    *      +---------------+---------------+---------------+---------------+
    *     8| Total body length                                             |
    *      +---------------+---------------+---------------+---------------+
    *    12| Opaque                                                        |
    *      +---------------+---------------+---------------+---------------+
    *    16| CAS                                                           |
    *      |                                                               |
    *      +---------------+---------------+---------------+---------------+
    *      @return ByteString represantion of a request header
    */
  def toByteString: ByteString = {
    longToByteString(magic.code,     1) ++
    longToByteString(opcode.code,    1) ++
    longToByteString(keyLength,      2) ++
    longToByteString(extrasLength,   1) ++
    longToByteString(0,              1) ++
    longToByteString(status.code,    2) ++
    longToByteString(totalBodyLength,4) ++
    longToByteString(opaque,         4) ++
    longToByteString(cas,            4)
  }
}
