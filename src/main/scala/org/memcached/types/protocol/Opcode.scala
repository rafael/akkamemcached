package org.memcached.types.protocol

import scala.util.{Failure, Success, Try}

/**
  * Created by rafael on 5/9/17.
  */

sealed trait  Opcode {
  def code: Int
}

//0x00    Get
case object Get extends Opcode {
  override val code = 0x00
}

//0x01    Set
case object Set extends Opcode {
  override val code = 0x01
}

//0x04    Delete
case object Delete extends Opcode {
  override val code = 0x04
}
//0x0B    Version
case object Version extends Opcode {
  override val code = 0x0B
}

object Opcode {
  /**
    * Constructs an opcode from an integer value
    * @param value integer value for an operation
    * @return
    */
  def apply(value: Int):Try[Opcode] =
    value match {
      case Get.code => Success(Get)
      case Set.code => Success(Set)
      case Delete.code  => Success(Delete)
      case Version.code => Success(Version)
      case _ => Failure(new RuntimeException(s"Invalid command: $value"))
    }
}


