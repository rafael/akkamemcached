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

//0x02    Add
case object Add extends Opcode {
  override val code = 0x02
}

//0x03    Replace
case object Replace extends Opcode {
  override val code = 0x03
}

//0x04    Delete
case object Delete extends Opcode {
  override val code = 0x04
}

//0x05    Increment
case object Increment extends Opcode {
  override val code = 0x05
}

//0x06    Decrement
case object Decrement extends Opcode {
  override val code = 0x06
}

//0x07    Quit
case object Quit extends Opcode {
  override val code = 0x07
}

//0x08    Flush
case object Flush extends Opcode {
  override val code = 0x08
}

//0x09    GetQ
case object GetQ extends Opcode {
  override val code = 0x09
}

//0x0A    No-op
case object Noop extends Opcode {
  override val code = 0x0A
}

//0x0B    Version
case object Version extends Opcode {
  override val code = 0x0B
}

//0x0C    GetK
case object GetK extends Opcode {
  override val code = 0x0C
}

//0x0D    GetKQ
case object GetKQ extends Opcode {
  override val code = 0x0D
}

//0x0E    Append
case object Append extends Opcode {
  override val code = 0x0E
}

//0x0F    Prepend
case object Prepend extends Opcode {
  override val code = 0x0F
}

//0x10    Stat
case object Stat extends Opcode {
  override val code = 0x10
}

//0x11    SetQ
case object SetQ extends Opcode {
  override val code = 0x11
}

//0x12    AddQ
case object AddQ extends Opcode {
  override val code = 0x12
}

//0x13    ReplaceQ
case object ReplaceQ extends Opcode {
  override val code = 0x13
}

//0x14    DeleteQ
case object DeleteQ extends Opcode {
  override val code = 0x14
}

//0x15    IncrementQ
case object IncrementQ extends Opcode {
  override val code = 0x15
}

//0x16    DecrementQ
case object DecrementQ extends Opcode {
  override val code = 0x16
}

//0x17    QuitQ
case object QuitQ extends Opcode {
  override val code = 0x17
}

//0x18    FlushQ
case object FlushQ extends Opcode {
  override val code = 0x18
}

//0x19    AppendQ
case object AppendQ extends Opcode {
  override val code = 0x19
}

//0x1A    PrependQ
case object PrependQ extends Opcode {
  override val code = 0x1A
}

object Opcode {
  def apply(value: Int):Try[Opcode] =
    value match {
      case Get.code => Success(Get)
      case Set.code => Success(Set)
      case Add.code => Success(Add)
      case Replace.code => Success(Replace)
      case Delete.code  => Success(Delete)
      case Increment.code => Success(Increment)
      case Decrement.code  => Success(Decrement)
      case Quit.code => Success(Quit)
      case Flush.code => Success(Flush)
      case GetQ.code  => Success(GetQ)
      case Noop.code => Success(Noop)
      case Version.code => Success(Version)
      case GetK.code => Success(GetK)
      case GetKQ.code => Success(GetKQ)
      case Append.code => Success(Append)
      case Prepend.code => Success(Prepend)
      case Stat.code => Success(Stat)
      case SetQ.code => Success(SetQ)
      case AddQ.code  => Success(AddQ)
      case ReplaceQ.code => Success(ReplaceQ)
      case DeleteQ.code  => Success(DeleteQ)
      case IncrementQ.code => Success(IncrementQ)
      case DecrementQ.code => Success(DecrementQ)
      case QuitQ.code => Success(QuitQ)
      case FlushQ.code => Success(FlushQ)
      case AppendQ.code => Success(AppendQ)
      case PrependQ.code => Success(PrependQ)
      case _ => Failure(new RuntimeException(s"Invalid command: $value"))
    }
}


