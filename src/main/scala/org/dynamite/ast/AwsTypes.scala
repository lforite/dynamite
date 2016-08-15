package org.dynamite.ast

import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JField, JObject, JString}

sealed trait AwsType
sealed trait AwsScalarType extends AwsType

case class S(value: String) extends AwsScalarType
case class N(value: String) extends AwsScalarType

private[dynamite] class AwsTypeSerializer extends CustomSerializer[AwsType](format => ( {
  case JObject(List(JField("S", JString(value)))) => S(value)
  case JObject(List(JField("N", JString(value)))) => N(value)
}, {
  case S(value) => JObject(JField("S", JString(value)))
  case N(value) => JObject(JField("N", JString(value)))
}))