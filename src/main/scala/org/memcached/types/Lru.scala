package org.memcached.types.caches

import akka.util.ByteString
import org.scalatest.prop.Configuration.MaxSize

import scala.collection.mutable

/**
  * Created by rafael on 5/12/17.
  */
abstract class SizeInBytes[A] {
  def size(x: A): Int
}

case class Node[A,B](key: A,
                     var value: B,
                     var pre: Option[Node[A,B]] = None,
                     var next: Option[Node[A,B]] = None)

/**
  * This LRU cache is implemented as LinkedHashMap. The linked list is updated every time there is an operation in the list.
  * The head of the list is the most recent used item. The tail is the oldest.
  * This was used as reference on how to implement the LRU cache:
  * https://alaindefrance.wordpress.com/2014/10/05/lru-cache-implementation/
  *
  * @param maxCacheSizeBytes Max value in bytes of this cache
  * @param itemMaxSizeInBytes Max value that an item could be
  * @param sizer implicit function that retrieves the size of the value
  * @tparam A Key Type
  * @tparam B Value Type
  */
case class Lru[A,B](maxCacheSizeBytes: Long, itemMaxSizeInBytes: Long)(implicit sizer: SizeInBytes[B]) {
  var hash: mutable.HashMap[A, Node[A,B]] = new mutable.HashMap()
  var tailOpt:Option[Node[A,B]] = None
  var headOpt:Option[Node[A,B]] = None
  var currentSize: Long = 0L

  /**
    * Fetches a key from the LRU
    * @param key
    * @return
    */
  def get(key: A): Option[B] = {
    hash.get(key) match {
      case Some(node) =>
        updateList(node)
        Some(node.value)
      case None =>
        None
    }
  }

  /**
    * Sets a key in the LRU
    * @param key to set
    * @param value to set
    */

  def set(key: A, value: B): Unit = {
    hash.get(key) match {
      case Some(oldValue) if sizer.size(value) <= itemMaxSizeInBytes =>
        currentSize -= sizer.size(oldValue.value)
        oldValue.value = value
        updateList(oldValue)
        cleanCache(sizer.size(value))
        currentSize += sizer.size(value)
      case None if sizer.size(value) <= itemMaxSizeInBytes =>
        val newNode = Node(key, value)
        cleanCache(sizer.size(value))
        currentSize += sizer.size(value)
        setHead(newNode)
        hash.put(key, newNode)
      case _ =>
        throw new RuntimeException("Attempting to insert an item that is bigger than max allowed size")
    }
  }

  /**
    * Deletes key from cache. Returns false if the key was not found
    * @param key
    * @return
    */

  def delete(key: A): Boolean = {
    hash.get(key) match {
      case Some(node) =>
        currentSize -= sizer.size(node.value)
        hash.remove(key)
        remove(node)
        true
      case _ => false
    }
  }

  private def updateList(node: Node[A, B]) = {
    remove(node)
    setHead(node)
  }

  private def setHead(node: Node[A,B]): Unit = {
    node.next = headOpt
    node.pre = None
    if (headOpt.isDefined)
      headOpt.get.pre = Some(node)
    headOpt = Some(node)
    if (tailOpt.isEmpty)
      tailOpt = headOpt
  }

  private def remove(node: Node[A,B]): Unit = {
    if (node.pre.isDefined)
      node.pre.get.next = node.next
    else
      headOpt = node.next
    if (node.next.isDefined)
      node.next.get.pre = node.pre
    else
      tailOpt = node.pre
  }

  private def cleanCache(newValueSize: Int) = {
    // This is nasty, but this method should never be called  if there is no tail,
    // so it's safe to assume that it will always be there.
    while (currentSize + newValueSize > maxCacheSizeBytes) {
     currentSize -= sizer.size(tailOpt.get.value)
     hash.remove(tailOpt.get.key)
     remove(tailOpt.get)
    }
  }
}
