package org.dynamite.ast

import org.json4s.JsonAST._

trait AwsJsonReader {
  protected[dynamite] def fromAws(json: JValue): JValue = shrinkObject(json)

  private val Names = Set("M", "SS")

  private def shrinkField(field: (String, JValue)): (String, JValue) = field match {
    case (name, JObject(List(JField("S", s: JString)))) => (name, s)
    case (name, JObject(List(JField("N", JString(n))))) => (name, JDecimal(BigDecimal(n)))
    case (name, JObject(List(JField("BOOL", b: JBool)))) => (name, b)
    case (name, JObject(List(JField("SS", ss: JArray)))) => (name, ss)
    case (name, JObject(List(JField("M", jo: JObject)))) => (name, shrinkObject(jo))
    case (name, JObject(List(JField("L", JArray(objs))))) => (name, JArray(objs map shrinkObject))
  }

  private def shrinkObject(json: JValue): JValue = json match {
    case JObject(List(JField(name, jv))) if Names contains name => shrinkObject(jv)
    case JObject(fields) => JObject(fields map shrinkField)
    case JArray(objs) => JArray(objs map shrinkObject)
    case value: JValue => value
  }

}

trait AwsJsonWriter {
  protected[dynamite] def toAws(json: JValue): JValue = augmentObject(json)

  private def augmentField(field: (String, JValue)): (String, JValue) = field match {
    case (name, js: JString) => (name, JObject(List("S" -> js)))
    case (name, JInt(i)) => (name, JObject(List("N" -> JString(i.toString))))
    case (name, JLong(d)) => (name, JObject(List("N" -> JString(d.toString))))
    case (name, JDecimal(d)) => (name, JObject(List("N" -> JString(d.toString))))
    case (name, jb: JBool) => (name, JObject(List("BOOL" -> jb)))
    case (name, ja@JArray(objs)) if objs.forall({ case js: JString => true case _ => false }) => (name, JObject(List("SS" -> ja)))
    case (name, jo: JObject) => (name, JObject(List("M" -> augmentObject(jo))))
    case (name, JArray(objs)) => (name, JObject(List("L" -> JArray(objs.map(o => JObject(List("M" -> augmentObject(o))))))))
  }

  private def augmentObject(json: JValue): JValue = json match {
    case JObject(l) => JObject(l map augmentField)
    case j: JValue => j
  }

}