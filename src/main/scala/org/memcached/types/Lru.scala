package org.memcached.types.caches

import akka.util.ByteString
import org.scalatest.prop.Configuration.MaxSize

import scala.collection.mutable

/**
  * Created by rafael on 5/12/17.
  */
case class Node(key: ByteString,
                var value: ByteString,
                var pre: Option[Node] = None,
                var next: Option[Node] = None)

case class Lru(maxCacheSizeBytes: Long, maxItemSizeBytes: Long, var currentSize: Long = 0L) {
  var hash: mutable.HashMap[ByteString, Node] = new mutable.HashMap()
  var tailOpt:Option[Node] = None
  var headOpt:Option[Node] = None

  def get(key: ByteString): Option[ByteString] = {
    hash.get(key) match {
      case Some(node) =>
        remove(node)
        setHead(node)
        Some(node.value)
      case None =>
        None
    }
  }

  def set(key: ByteString, value: ByteString): Unit = {
    (hash.get(key), tailOpt) match {
      case (Some(oldValue), _) if value.size <= maxItemSizeBytes =>
        currentSize -= oldValue.value.size
        oldValue.value = value
        remove(oldValue)
        setHead(oldValue)
        cleanCache(value.size)
        currentSize += value.size
      case (None, Some(_)) if value.size <= maxItemSizeBytes =>
        val newNode = Node(key, value)
        cleanCache(value.size)
        currentSize += value.size
        setHead(newNode)
        hash.put(key, newNode)
      case (None, None) if value.size <= maxItemSizeBytes =>
        val newNode = Node(key, value)
        currentSize += value.size
        setHead(newNode)
        hash.put(key, newNode)
      case _ =>
        throw new RuntimeException("Attempting to insert an item that is bigger than max allowed size")
    }
  }

  def delete(key: ByteString): Boolean = {
    hash.get(key) match {
      case Some(node) =>
        currentSize -= node.value.size
        hash.remove(key)
        remove(node)
        true
      case _ => false
    }
  }

  private def setHead(node: Node): Unit = {
    node.next = headOpt
    node.pre = None
    if (headOpt.isDefined)
      headOpt.get.pre = Some(node)
    headOpt = Some(node)
    if (tailOpt.isEmpty)
      tailOpt = headOpt
  }

  private def remove(node: Node): Unit = {
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
     currentSize -= tailOpt.get.value.size
     hash.remove(tailOpt.get.key)
     remove(tailOpt.get)
    }
  }
}
