package org.memcached.types

/**
  * Created by rafael on 5/9/17.
  */

sealed trait ResponseStatus {
  def code: Int
}

// 0x0000  No error
case object NoError extends  ResponseStatus {
  override val code = 0x0
}
// 0x0001  Key not found
case object KeyNotFound extends  ResponseStatus {
  override val code = 0x1

}
// 0x0002  Key exists
case object KeyExists extends ResponseStatus {
  override val code = 0x2
}

// 0x0003  Value too large
case object ValueTooLarge extends ResponseStatus {
  override val code = 0x3
}

//0x0004  Invalid arguments
case object InvalidArguments extends ResponseStatus {
  override val code = 0x4
}

//0x0005  Item not stored
case object ItemNotStored extends ResponseStatus {
  override val code = 0x5
}

//0x0006  Incr/Decr on non-numeric value.
case object IncrDecr extends  ResponseStatus {
  override val code = 0x6
}

//0x0081  Unknown command
case object UnknownCommand extends  ResponseStatus {
  override val code = 0x0081
}

//0x0082  Out of memory
case object OutOfMemory extends  ResponseStatus {
  override val code = 0x0082
}

