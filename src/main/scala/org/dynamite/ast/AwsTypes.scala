package org.dynamite.ast

import dynamo.ast._
import org.json4s.CustomSerializer
import org.json4s.JsonAST._

private [dynamite] case class ROOT(dynamoType: DynamoType)

private[dynamite] class ROOTTypeSerializer extends CustomSerializer[ROOT](format => (ROOTTypeSerializer.JValueToROOT, ROOTTypeSerializer.ROOTToJValue))

object ROOTTypeSerializer {
  val JValueToROOT: PartialFunction[JValue, ROOT] = {
    case JObject(elems) => ROOT(M(elems.map(kv => kv._1 -> DynamoTypeSerializer.JValueToAwsDynamoType(kv._2))))
  }

  val ROOTToJValue: PartialFunction[Any, JValue] = {
    case ROOT(M(elements)) => JObject(elements.map(kv => kv._1 -> DynamoTypeSerializer.AwsDynamoTypeToJValue(kv._2)))
  }
}

private[dynamite] class DynamoTypeSerializer extends CustomSerializer[DynamoType](format => (DynamoTypeSerializer.JValueToAwsDynamoType, DynamoTypeSerializer.AwsDynamoTypeToJValue))

object DynamoTypeSerializer {
  val SPf: PartialFunction[JValue, S] = {
    case JString(s) => S(s)
  }

  val NPf: PartialFunction[JValue, N] = {
    case JString(n) => N(n)
  }

  val JValueToAwsDynamoType: PartialFunction[JValue, DynamoType] = {
    case JObject(List(JField("S", JString(value)))) => S(value)
    case JObject(List(JField("N", JString(value)))) => N(value)
    case JObject(List(JField("BOOL", JBool(value)))) => BOOL(value)
    case JObject(List(JField("L", JArray(elems)))) => L(elems map JValueToAwsDynamoType)
    case JObject(List(JField("M", JObject(kvs)))) => M(kvs map (kv => kv._1 -> JValueToAwsDynamoType(kv._2)))
    case JObject(List(JField("NS", JArray(elems)))) => NS(elems map NPf toSet)
    case JObject(List(JField("SS", JArray(elems)))) => SS(elems map SPf toSet)
    case JObject(List(JField("NULL", _))) => NULL
  }

  val AwsDynamoTypeToJValue: PartialFunction[Any, JValue] = {
    case S(value) => JObject("S" -> JString(value))
    case N(value) => JObject("N" -> JString(value))
    case BOOL(value) => JObject("BOOL" -> JBool(value))
    case L(values) => JObject(JField("L", JArray(values map AwsDynamoTypeToJValue)))
    case M(kvs) => JObject(JField("M", JObject(kvs map (kv => kv._1 -> AwsDynamoTypeToJValue(kv._2)))))
    case NS(values) => JObject(JField("NS", JArray(values.map(n => JString(n.value)) toList)))
    case SS(values) => JObject(JField("SS", JArray(values.map(s => JString(s.value)) toList)))
    case NULL => JObject(JField("NULL", JBool(true)))
  }
}
