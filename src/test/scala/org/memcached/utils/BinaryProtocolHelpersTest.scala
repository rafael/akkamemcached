package org.memcached.utils

import akka.util.ByteString
import org.specs2.mutable.Specification

/**
  * Created by rafael on 5/10/17.
  */
class  BinaryProtocolHelpersTest extends Specification {
  import BinaryProtocolHelpers._

  "Read number 1 byte number" >> {
    val x = readLong(ByteString(Array(0x8.toByte)))
    x must_== 0x8
  }

  "Read number greater than two byte number" >> {
    val x = readLong(ByteString(Array(0x8.toByte, 0x8.toByte, 0x8.toByte)))
    x must_== 0x080808
  }


}
