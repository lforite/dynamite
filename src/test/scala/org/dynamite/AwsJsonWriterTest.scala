package org.dynamite

import org.json4s.JsonAST._
import org.specs2.Specification

class AwsJsonWriterTest extends Specification { def is = s2"""
 Specification for the AwsJsonReader
   JString are represented as S objects $augmentJString
   JInt are represented as N objects $augmentJInt
   JDecimal are represented as N objects $augmentJDecimal
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
