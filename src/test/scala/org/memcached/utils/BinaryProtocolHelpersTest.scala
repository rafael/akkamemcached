package org.memcached.utils

import akka.util.ByteString
import org.specs2.mutable.Specification

/**
  * Created by rafael on 5/10/17.
  */
class  BinaryProtocolHelpersTest extends Specification {
  import BinaryProtocolHelpers._
  "ResponseHeaderTest byteStringToLong" should {
    "read number 1 byte number" in {
      val x = byteStringToLong(ByteString(Array(0x8.toByte)))
      x mustEqual 0x8
    }
    "read number 1 byte number" in {
      val x = byteStringToLong(ByteString(Array(0x8.toByte)))
      x mustEqual 0x8
    }
    "read number greater than 1 byte number" in {
      val x = byteStringToLong(ByteString(Array(0x8.toByte, 0x7.toByte, 0x6.toByte)))
      x mustEqual  0x080706
    }

    "convert Long to byteString with 1 byte" in {
      val byteArray = ByteString(Array(0x8.toByte))
      val x = longToByteString(0x8, 1)
      x mustEqual byteArray
    }

    "convert Long to byteString with target more than one byte" in {
      val byteArray = ByteString(Array(0x8.toByte, 0x7.toByte, 0x6.toByte))
      val x = longToByteString(0x080706, 3)
      x mustEqual byteArray
    }

    "convert Long to byteString with target having leading 0" in {
      val byteArray = ByteString(Array(0x0.toByte, 0x0.toByte, 0x8.toByte))
      val x = longToByteString(0x000008, 3)
      x mustEqual byteArray
    }
  }
}
