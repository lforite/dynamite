package org.dynamite.dsl

import org.dynamite.ast.DynamoTypeSerializer
import org.json4s.JsonAST.JValue
import org.json4s.{DefaultFormats, Formats}

import scalaz.\/

object Format {
  implicit val defaultFormats: Formats =  DefaultFormats + new AwsErrorSerializer + new DynamoTypeSerializer
}

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
