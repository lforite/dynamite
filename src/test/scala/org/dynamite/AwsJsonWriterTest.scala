package org.dynamite

import org.json4s.JsonAST._
import org.specs2.Specification

class AwsJsonWriterTest extends Specification { def is = s2"""
 Specification for the AwsJsonReader
   JString are represented as S objects $augmentJString
   JInt are represented as N objects $augmentJInt
   JDecimal are represented as N objects $augmentJDecimal
   JLong are represented as N objects $augmentJLong
   JBool are represented as BOOL objects $augmentJBool
   JArray of JString are represented as SS objects $augmentJArrayString
   JObject are represented as M objects $augmentJObject
   JArray of JObject are represented as L objects $augmentJArrayObject
   Json is augmented with Aws noise                 $toAws
  """

  def augmentJString = {
    val json = JObject(List(
      "sField" -> JString("s value")))

    val expected = JObject(List(
      "sField" -> JObject(List("S" -> JString("s value")))))

    Dummy.toAws(json) must be_==(expected)
  }

  def augmentJInt = {
    val json = JObject(List(
      "nField" -> JInt(1)))

    val expected = JObject(List(
      "nField" -> JObject(List("N" -> JString("1")))))

    Dummy.toAws(json) must be_==(expected)
  }

  def augmentJDecimal = {
    val json = JObject(List(
      "nField" -> JDecimal(12.0)))

    val expected = JObject(List(
      "nField" -> JObject(List("N" -> JString("12.0")))))

    Dummy.toAws(json) must be_==(expected)
  }

  def augmentJLong = {
    val json = JObject(List(
      "nField" -> JLong(12L)))

    val expected = JObject(List(
      "nField" -> JObject(List("N" -> JString("12")))))

    Dummy.toAws(json) must be_==(expected)
  }

  def augmentJBool = {
    val json = JObject(List(
      "bField" -> JBool(true)))

    val expected = JObject(List(
      "bField" -> JObject(List("BOOL" -> JBool(true)))))

    Dummy.toAws(json) must be_==(expected)
  }

  def augmentJArrayString = {
    val json = JObject(List(
      "aField" -> JArray(List(JString("a"), JString("b")))))

    val expected = JObject(List(
      "aField" -> JObject(List("SS" -> JArray(List(JString("a"), JString("b")))))))

    Dummy.toAws(json) must be_==(expected)
  }

  def augmentJObject = {
    val json = JObject(List(
      "oField" -> JObject(List("inner" -> JString("hello !")))))

    val expected = JObject(List(
      "oField" -> JObject(List("M" -> JObject(List("inner" -> JObject(List("S" -> JString("hello !")))))))))

    Dummy.toAws(json) must be_==(expected)
  }

  def augmentJArrayObject = {
    val json = JObject(List(
      "aField" -> JArray(List(JObject(List("inner" -> JString("hello !")))))))

    val expected = JObject(List(
      "aField" -> JObject(List("L" ->
        JArray(List(
          JObject(List("M" -> JObject(List("inner" -> JObject(List("S" -> JString("hello !")))))))
        ))))))

    Dummy.toAws(json) must be_==(expected)
  }

  def toAws = {
    val json = JObject(List(
      "sField" -> JString("s value"),
      "nField" -> JInt(1)))

    val expected = JObject(List(
      "sField" -> JObject(List("S" -> JString("s value"))),
      "nField" -> JObject(List("N" -> JString("1")))))

    Dummy.toAws(json) must be_==(expected)
  }

  private[this] object Dummy extends AwsJsonWriter

}
