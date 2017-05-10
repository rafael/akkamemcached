package org.memcached.types

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


