package org.dynamite.ast

import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JField, JObject, JString}

sealed trait AwsType
sealed trait AwsScalarType extends AwsType

/**
  * An attribute of type String
  */
case class S(value: String) extends AwsScalarType

/**
  * An attribute of type Number
  */
case class N(value: String) extends AwsScalarType

/**
  * An attribute of type Boolean
  */
case class BOOL(boolean: Boolean) extends AwsScalarType

/**
  * An attribute of type List
  */
case class L(elements: AwsType) extends AwsType

/**
  * An attribute of type Map
  */
case class M(elems: (String, AwsType)*) extends AwsType

/**
  * An attribute of type Number
  */
case class NS(numbers: Set[N]) extends AwsType

/**
  * An attribute of type Null
  */
case object NULL extends AwsType

/**
  * An attribute of type String Set
  */
case class SS(strings: Set[S]) extends AwsType

private[dynamite] class AwsTypeSerializer extends CustomSerializer[AwsType](format => ( {
  case JObject(List(JField("S", JString(value)))) => S(value)
  case JObject(List(JField("N", JString(value)))) => N(value)
}, {
  case S(value) => JObject(JField("S", JString(value)))
  case N(value) => JObject(JField("N", JString(value)))
}))