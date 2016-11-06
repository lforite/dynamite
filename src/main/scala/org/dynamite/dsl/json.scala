package org.dynamite.dsl

import org.json4s._

import scalaz.\/

private[dynamite] trait JsonSerializable[A] {
  def serialize(a: A): DynamoCommonError \/ RequestBody
}

private[dynamite] object JsonSerializable {
  def apply[A](implicit ev: JsonSerializable[A]) = ev
}

private[dynamite] trait JsonDeserializable[A] {
  def deserialize(jValue: JValue): A
}

private[dynamite] object JsonDeserializable {
  def apply[A](implicit ev: JsonDeserializable[A]) = ev
}