package org.memcached.utils

import akka.util.ByteString
import org.memcached.types._
import org.memcached.types.protocol._

import scala.util.{Failure, Success, Try}

/**
  * Created by rafael on 5/10/17.
  */
object BinaryProtocolHelpers {

  lazy val versionResponse:ByteString = ResponseHeader(
    opcode = Version,
    keyLength = 0,
    extrasLength = 0,
    status = NoError,
    totalBodyLength = 5,
    opaque = 0,
    cas = 0
  ).toByteString ++ ByteString("1.3.1".getBytes())

  def buildResponseNoBody(opd: Opcode, cas: Long): ByteString =
    ResponseHeader(opd, 0, 0, NoError, 0, 0, cas).toByteString

  def buildErrorResponse(status: ResponseStatus, requestOpcode: Opcode): ByteString = {
    val msgBytes = ByteString(status.msg.getBytes)
    ResponseHeader(requestOpcode, 0, 0, status, msgBytes.size, 0, 0).toByteString ++ msgBytes
  }

  def buildGetRequestResponse(opaque: Long,
                              payload: ByteString,
                              cas: Long,
                              flags: ByteString): ByteString =
    ResponseHeader(
      opcode = Get,
      keyLength = 0,
      extrasLength = 4,
      status = NoError,
      totalBodyLength  = payload.size + 4,
      opaque = opaque,
      cas = cas
    ).toByteString ++ flags ++ payload

  def parseServerCmd(request: RequestHeader, payload: ByteString): Try[ServerCmd] = {
    request.opcode match {
      //  Get command doesn't have anything in Extras area
      case Get => Success(GetCmd(payload.slice(24 + request.extrasLength,24 + request.extrasLength + request.keyLength)))
      case Set =>
        val key = payload.slice(24 + request.extrasLength, 24 + request.extrasLength + request.keyLength)
        val flags = payload.slice(24, 28)
        val value = payload.slice(
          24 + request.extrasLength + request.keyLength,
          24 + request.totalBodyLength.toInt)
        Success(SetCmd(key, value, request.cas, flags))
      case Version => Success(VersionCmd)
      case Delete =>
        val key = payload.slice(24 + request.extrasLength, 24 + request.extrasLength + request.keyLength)
        Success(DeleteCmd(key))
      case _ => Failure(new RuntimeException("Unsupported Command"))
    }
  }

  def byteStringToLong(bytes: ByteString):Long =
    (bytes.size-1 to 0 by -1).foldLeft(0L) { (acc, i) =>
      // we shift each byte 8 bits to the left to add it
      // at the proper place. We also apply a mask because
      // by default bytes are signed in Java.
      acc | ((bytes(i) & 0xFF) << (bytes.size-1-i) * 8)
    }

  def longToByteString(number: Long, targetBytes: Int): ByteString = {
    val byteArray: Array[Byte] = (targetBytes-1 to 0 by -1).toArray.map(i => (number >>> i * 8).toByte)
    ByteString(byteArray)
  }

}
