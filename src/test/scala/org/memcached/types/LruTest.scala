package org.memcached.types

import akka.util.ByteString
import org.specs2.mutable.Specification

/**
  * Created by rafael on 5/12/17.
  */
class LruTest extends Specification {
  implicit object Sizer extends SizeInBytes[ByteString] {
    def size(x: ByteString ): Int = x.size
  }

  "LruTest" should {
    "currentSize returns 0 when the cache is empty" in {
      val cache = Lru[ByteString, ByteString](10, 100)
      cache.currentSize mustEqual 0
    }

    "sets the value in the cache" in {

      val cache = Lru[ByteString, ByteString](10, 100)
      cache.set(ByteString("a".getBytes()), ByteString("b".getBytes()))
      cache.get(ByteString("a".getBytes())) must beSome(ByteString("b".getBytes()))
    }

    "sets the value in the cache" in {
      val cache = Lru[ByteString, ByteString](10, 100)
      cache.set(ByteString("a".getBytes()), ByteString("b".getBytes()))
      cache.get(ByteString("a".getBytes())) must beSome(ByteString("b".getBytes()))
    }

    "sets updates current size value" in {
      val cache = Lru[ByteString, ByteString](10, 100)
      cache.set(ByteString("a".getBytes()), ByteString("ccc".getBytes()))
      cache.currentSize mustEqual 3
    }

    "sets replaces existent element and updates current size value" in {
      val cache = Lru[ByteString, ByteString](10, 100)
      cache.set(ByteString("a".getBytes()), ByteString("ccc".getBytes()))
      cache.currentSize mustEqual 3
      cache.set(ByteString("a".getBytes()), ByteString("c".getBytes()))
      cache.currentSize mustEqual 1
    }

     "set removes oldest element when cache is full" in {
      val cache = Lru[ByteString, ByteString](2, 2)
      cache.set(ByteString("a".getBytes()), ByteString("cc".getBytes()))
      cache.set(ByteString("b".getBytes()), ByteString("c".getBytes()))
      cache.currentSize mustEqual 1
      cache.get(ByteString("a".getBytes())) must beNone
      cache.get(ByteString("b".getBytes())) must beSome(ByteString("c".getBytes()))
    }

    "set removes elements when cache is full and new item is using an existent key" in {
      val cache = Lru[ByteString, ByteString](4, 4)
      cache.set(ByteString("a".getBytes()), ByteString("a".getBytes()))
      cache.set(ByteString("b".getBytes()), ByteString("b".getBytes()))
      cache.set(ByteString("c".getBytes()), ByteString("c".getBytes()))
      cache.set(ByteString("d".getBytes()), ByteString("d".getBytes()))
      cache.currentSize mustEqual 4
      cache.set(ByteString("a".getBytes()), ByteString("abcd".getBytes()))
      cache.get(ByteString("a".getBytes())) must beSome(ByteString("abcd".getBytes()))
      cache.get(ByteString("b".getBytes())) must beNone
      cache.get(ByteString("c".getBytes())) must beNone
      cache.get(ByteString("d".getBytes())) must beNone
      cache.currentSize mustEqual 4
    }

    "set removes oldest elements when cache is full and new item is using a new key" in {
      val cache = Lru[ByteString, ByteString](4, 4)
      cache.set(ByteString("a".getBytes()), ByteString("a".getBytes()))
      cache.set(ByteString("b".getBytes()), ByteString("b".getBytes()))
      cache.set(ByteString("c".getBytes()), ByteString("c".getBytes()))
      cache.set(ByteString("d".getBytes()), ByteString("d".getBytes()))
      cache.currentSize mustEqual 4
      cache.set(ByteString("x".getBytes()), ByteString("abcd".getBytes()))
      cache.get(ByteString("x".getBytes())) must beSome(ByteString("abcd".getBytes()))
      cache.get(ByteString("b".getBytes())) must beNone
      cache.get(ByteString("c".getBytes())) must beNone
      cache.get(ByteString("d".getBytes())) must beNone
      cache.currentSize mustEqual 4
    }

    "set removes items by oldest first" in {
      val cache = Lru[ByteString, ByteString](4, 1)
      cache.set(ByteString("a".getBytes()), ByteString("a".getBytes()))
      cache.set(ByteString("b".getBytes()), ByteString("b".getBytes()))
      cache.set(ByteString("c".getBytes()), ByteString("c".getBytes()))
      cache.set(ByteString("d".getBytes()), ByteString("d".getBytes()))

      cache.currentSize mustEqual 4

      cache.set(ByteString("e".getBytes()), ByteString("e".getBytes()))
      cache.get(ByteString("e".getBytes())) must beSome(ByteString("e".getBytes()))

      cache.currentSize mustEqual 4

      cache.get(ByteString("a".getBytes())) must beNone
      cache.set(ByteString("f".getBytes()), ByteString("f".getBytes()))
      cache.get(ByteString("f".getBytes())) must beSome(ByteString("f".getBytes()))
      cache.currentSize mustEqual 4

      cache.get(ByteString("b".getBytes())) must beNone

      cache.set(ByteString("g".getBytes()), ByteString("g".getBytes()))
      cache.get(ByteString("g".getBytes())) must beSome(ByteString("g".getBytes()))
      cache.currentSize mustEqual 4

      cache.get(ByteString("c".getBytes())) must beNone

      cache.set(ByteString("h".getBytes()), ByteString("h".getBytes()))
      cache.get(ByteString("h".getBytes())) must beSome(ByteString("h".getBytes()))
      cache.currentSize mustEqual 4
      cache.get(ByteString("d".getBytes())) must beNone


      cache.get(ByteString("e".getBytes())) must beSome(ByteString("e".getBytes()))
      cache.get(ByteString("f".getBytes())) must beSome(ByteString("f".getBytes()))
      cache.get(ByteString("g".getBytes())) must beSome(ByteString("g".getBytes()))
      cache.currentSize mustEqual 4
    }

    "get makes item the most recent in the cache" in {
      val cache = Lru[ByteString, ByteString](3, 1)
      cache.set(ByteString("a".getBytes()), ByteString("a".getBytes()))
      cache.set(ByteString("b".getBytes()), ByteString("b".getBytes()))
      cache.set(ByteString("c".getBytes()), ByteString("c".getBytes()))
      cache.get(ByteString("a".getBytes())) must beSome
      cache.set(ByteString("h".getBytes()), ByteString("h".getBytes()))
      cache.get(ByteString("a".getBytes())) must beSome
      cache.get(ByteString("b".getBytes())) must beNone
      cache.get(ByteString("c".getBytes())) must beSome
    }



    "get returns none when the value is not in the cache" in {
      val cache = Lru[ByteString, ByteString](10, 100)
      cache.get(ByteString("get".getBytes())) must beNone
    }

    "delete removes element from cache" in {
      val cache = Lru[ByteString, ByteString](10, 100)
      cache.set(ByteString("a".getBytes()), ByteString("b".getBytes()))
      cache.get(ByteString("a".getBytes())) must beSome(ByteString("b".getBytes()))
      cache.delete(ByteString("a".getBytes())) must beTrue
      cache.get(ByteString("a".getBytes())) must beNone
    }

    "delete returns false when removing an unexistent item" in {
      val cache = Lru[ByteString, ByteString](10, 100)
      cache.delete(ByteString("a".getBytes())) must beFalse
    }
  }
}
