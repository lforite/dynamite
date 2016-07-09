package org.dynamite

import org.json4s.JsonAST._

trait AwsJsonReader {

  def fromAws(json: JValue): JValue = shrinkObject(json)

  private def shrinkField(field: (String, JValue)): (String, JValue) = field match {
    case (name, JObject(List(JField("S", s: JString)))) => (name, s)
    case (name, JObject(List(JField("N", JString(n))))) => (name, JDecimal(BigDecimal(n)))
    case (name, JObject(List(JField("BOOL", b: JBool)))) => (name, b)
    case (name, JObject(List(JField("SS", ss: JArray)))) => (name, ss)
    case (name, JObject(List(JField("L", l: JArray)))) => (name, l map shrinkObject)
    case (name, JObject(List(JField("M", m: JObject)))) => (name, shrinkObject(m))
  }

  private def shrinkObject(json: JValue): JValue = json match {
    case JObject(l) => JObject(l map shrinkField)
    case JArray(arr) => JArray(arr map shrinkObject)
    case value: JValue => value
  }

}


trait AwsJsonWriter {
  def toAws(json: JValue): JValue = augmentObject(json)

  private def augmentField(field: (String, JValue)): (String, JValue) = field match {
    case (name, js: JString) => (name, JObject(List(JField("S", js))))
    case (name, JInt(i)) => (name, JObject(List(JField("N", JString(i.toString)))))
    case (name, JLong(d)) => (name, JObject(List(JField("N", JString(d.toString)))))
    case (name, JDecimal(d)) => (name, JObject(List(JField("N", JString(d.toString)))))
    case (name, jb: JBool) => (name, JObject(List(JField("BOOL", jb))))
    case (name, JArray(objs)) if objs.forall({ case js: JString => true case _ => false }) => (name, JObject(List(JField("SS", JArray(objs)))))
  }

  private def augmentObject(json: JValue): JValue = json match {
    case JObject(l) => JObject(l map augmentField)
  }

}