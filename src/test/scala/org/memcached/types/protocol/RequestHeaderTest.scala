package org.memcached.types.protocol

import org.specs2.mutable.Specification

import scala.util.Success

/**
  * Created by rafael on 5/10/17.
  */
class RequestHeaderTest extends Specification {
  "RequestHeahderTest toByteString" should {
    "produce a correct valid binary representation of the message" in {
      val msg = RequestHeader(
        opcode = Get,
        keyLength = 1,
        extrasLength = 1,
        reserved = 1,
        totalBodyLength = 1,
        opaque = 1,
        cas = 1
      )
      RequestHeader(msg.toByteString) mustEqual Success(msg)
    }
  }
}
