package org.dynamite.dsl

import io.circe.Json

import scalaz.\/

private[dynamite] trait JsonSerializable[A] {
  def serialize(a: A): DynamoCommonError \/ RequestBody
}

private[dynamite] object JsonSerializable {
  def apply[A](implicit ev: JsonSerializable[A]) = ev
}

private[dynamite] trait JsonDeserializable[A] {
  def deserialize(json: Json): A
}

private[dynamite] object JsonDeserializable {
  def apply[A](implicit ev: JsonDeserializable[A]) = ev
}
