package org.dynamite.ast

import org.json4s.CustomSerializer
import org.json4s.JsonAST._

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
case class BOOL(value: Boolean) extends AwsScalarType

/**
  * An attribute of type List
  */
case class L(elements: List[AwsType]) extends AwsType

/**
  * An attribute of type Map
  */
case class M(elems: List[(String, AwsType)]) extends AwsType

/**
  * An attribute of type Number
  */
case class NS(numbers: Set[N]) extends AwsType

/**
  * An attribute of type String Set
  */
case class SS(strings: Set[S]) extends AwsType

/**
  * An attribute of type Null
  */
case object NULL extends AwsType


private[dynamite] class AwsTypeSerializer extends CustomSerializer[AwsType](format => (AwsTypeSerializer.JValueToAwsType, AwsTypeSerializer.AwsTypeToJValue))

object AwsTypeSerializer {
  val SPf: PartialFunction[JValue, S] = {
    case JString(s) => S(s)
  }

  val NPf: PartialFunction[JValue, N] = {
    case JString(n) => N(n)
  }

  val JValueToAwsType: PartialFunction[JValue, AwsType] = {
    case JObject(List(JField("S", JString(value)))) => S(value)
    case JObject(List(JField("N", JString(value)))) => N(value)
    case JObject(List(JField("BOOL", JBool(value)))) => BOOL(value)
    case JObject(List(JField("L", JArray(elems)))) => L(elems map JValueToAwsType)
    case JObject(List(JField("M", JObject(kvs)))) => M(kvs map (kv => kv._1 -> JValueToAwsType(kv._2)))
    case JObject(List(JField("NS", JArray(elems)))) => NS(elems map NPf toSet)
    case JObject(List(JField("SS", JArray(elems)))) => SS(elems map SPf toSet)
    case JObject(List(JField("NULL", _))) => NULL
  }

  val AwsTypeToJValue: PartialFunction[Any, JValue] = {
    case S(value) => JObject("S" -> JString(value))
    case N(value) => JObject("N" -> JString(value))
    case BOOL(value) => JObject("BOOL" -> JBool(value))
    case L(values) => JObject(JField("L", JArray(values map AwsTypeToJValue)))
    case M(kvs) => JObject(JField("M", JObject(kvs map (kv => kv._1 -> AwsTypeToJValue(kv._2)))))
    case NS(values) => JObject(JField("NS", JArray(values map AwsTypeToJValue toList)))
    case SS(values) => JObject(JField("SS", JArray(values map AwsTypeToJValue toList)))
    case NULL => JObject(JField("NULL", JBool(true)))
  }
}