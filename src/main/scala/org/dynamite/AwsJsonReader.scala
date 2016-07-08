package org.dynamite

import org.json4s.JsonAST._

import scala.collection.generic.SeqFactory

trait AwsJsonReader {

  def fromAws(json: JValue): JValue = shrinkObject(json)

  private def shrinkField(field: (String, JValue)): (String, JValue) = field match {
    case (name, JObject(List(JField("S", s: JString)))) => (name, s)
    case (name, JObject(List(JField("N", n: JString)))) => (name, n)
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
  }

  private def augmentObject(json: JValue): JValue = json match {
    case JObject(l) => JObject(l map augmentField)
  }

}