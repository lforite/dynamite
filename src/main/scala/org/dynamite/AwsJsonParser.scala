package org.dynamite

import org.json4s.JsonAST._

trait AwsJsonParser {

  def fromAws(json: JValue): JValue = shrinkObject(json)

  private def shrinkField(field: (String, JValue)): (String, JValue) = field match {
    case (name, JObject(List(JField("S", s: JString)))) => (name, s)
    case (name, JObject(List(JField("N", s: JString)))) => (name, s)
    case (name, JObject(List(JField("BOOL", s: JBool)))) => (name, s)
    case (name, JObject(List(JField("SS", s: JArray)))) => (name, s)
    case (name, JObject(List(JField("L", s: JArray)))) => (name, s map shrinkObject)
  }

  private def shrinkObject(json: JValue): JValue = json match {
    case JObject(l) => JObject(l map shrinkField)
    case JArray(arr) => JArray(arr map shrinkObject)
    case value: JValue => value
  }

}
