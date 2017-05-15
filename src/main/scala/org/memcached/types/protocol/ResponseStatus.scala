package org.memcached.types.protocol

/**
  * Created by rafael on 5/9/17.
  */

sealed trait ResponseStatus {
  def code: Int
  def msg: String
}

// 0x0000  No error
case object NoError extends  ResponseStatus {
  override val code = 0x0
  override val msg = "No error"
}
// 0x0001  Key not found
case object KeyNotFound extends  ResponseStatus {
  override val code = 0x1
  override val msg = "Not found"

}
// 0x0002  Key exists
case object KeyExists extends ResponseStatus {
  override val code = 0x2
  override val msg = "Key exists"
}

// 0x0003  Value too large
case object ValueTooLarge extends ResponseStatus {
  override val code = 0x3
  override val msg = "Value too large"
}

//0x0004  Invalid arguments
case object InvalidArguments extends ResponseStatus {
  override val code = 0x4
  override val msg = "Invalid arguments"
}

//0x0005  Item not stored
case object ItemNotStored extends ResponseStatus {
  override val code = 0x5
  override val msg = "Item not stored"
}

//0x0006  Incr/Decr on non-numeric value.
case object IncrDecr extends  ResponseStatus {
  override val code = 0x6
  override val msg = "Incr/Decr on non-numeric value."
}

//0x0081  Unknown command
case object UnknownCommand extends  ResponseStatus {
  override val code = 0x81
  override val msg = "Unknown command"
}

//0x0082  Out of memory
case object OutOfMemory extends  ResponseStatus {
  override val code = 0x82
  override val msg = "OOM"
}

