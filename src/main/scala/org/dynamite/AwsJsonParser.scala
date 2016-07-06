package org.dynamite

import org.json4s.JsonAST._

trait AwsJsonParser {

  def fromAws(json: JValue): JValue = shrinkObject(json)

  def toAws(json: JValue): JValue = ???

  private def shrinkField(t: (String, JValue)): (String, JValue) = {
    t match {
      case (name, JObject(List(JField("S", s: JString)))) => (name, s)
      case (name, JObject(List(JField("N", s: JString)))) => (name, s)
      case (name, JObject(List(JField("BOOL", s: JBool)))) => (name, s)
      case (name, JObject(List(JField("SS", s: JArray)))) => (name, s)
    }
  }

  private def shrinkObject(json: JValue): JValue = {
    json match {
      case JObject(l) => JObject(l map shrinkField)
      case JArray(arr) => JArray(arr map shrinkObject)
    }
  }

}
