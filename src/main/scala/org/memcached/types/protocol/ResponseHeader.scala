package org.memcached.types.protocol

/**
  * Created by rafael on 5/10/17.
  */
/**
  * Data Type to represent a response header in Memcached Binary protocol
  * <p>
  * @param  opcode Command code.
  * @param  keyLength Length in bytes of the text key that follows the command extras.
  * @param  extrasLength  Length in bytes of the command extras.
  * @param  dataType  Reserved for future use (Sean is using this soon). (WHO IS SEAN)???
  * @param  status  Status of the response (non-zero on error).
  * @param  totalBodyLength Length in bytes of extra + key + value.
  * @param  opaque Will be copied back to you in the response.
  * @param  cas Data version check.
  */
case class ResponseHeader(
                           opcode: Int,
                           keyLength: Int,
                           extrasLength: Int,
                           dataType: Int,
                           status: ResponseStatus,
                           totalBodyLength: Int,
                           opaque: Int,
                           cas: Int
                         ) {
  // Per protocol version definition for ResponsePacket header
  val magic = ResponsePacketMagic
}
