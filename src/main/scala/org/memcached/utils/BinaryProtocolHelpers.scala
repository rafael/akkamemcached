package org.memcached.utils

import akka.util.ByteString

/**
  * Created by rafael on 5/10/17.
  */
object BinaryProtocolHelpers {

  def byteStringToLong(bytes: ByteString):Long =
    (bytes.size-1 to 0 by -1).foldLeft(0L) { (acc, i) =>
      // we shift each byte 8 bits to the left to add it
      // at the proper place
      acc | ((bytes(i) & 0xFF) << (bytes.size-1-i) * 8)
    }

  def longToByteString(number: Long, targetBytes: Int): ByteString = {
    val byteArray: Array[Byte] = (targetBytes-1 to 0 by -1).toArray.map(i => (number >>> i * 8).toByte)
    ByteString(byteArray)
  }

}
