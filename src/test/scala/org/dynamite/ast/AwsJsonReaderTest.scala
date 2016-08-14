package org.dynamite.ast

import org.json4s.JsonAST._
import org.specs2._

class AwsJsonReaderTest extends Specification { def is = s2"""
 Specification for the AwsJsonReader
   S fields are correctly shrunk $shrinkSField
   N fields are correctly shrunk $shrinkNField
   BOOL fields are correctly shrunk $shrinkSsField
   SS fields are correctly shrunk $shrinkBoolField
   L fields are correctly shrunk $shrinkLField
   M fields are correctly shrunk $shrinkMField
   Aws noise is swallowed by the reader                 $fromAws
  """

  def shrinkSField = {
    val sField: JValue = JObject(List(JField("S", JString("s value"))))
    val aws: JValue = JObject(List(
      JField("sField", sField)))

    val expected: JValue = JObject(List(
      JField("sField", JString("s value"))))

    AwsJsonReader.fromAws(aws) must be_==(expected)
  }

  def shrinkNField = {
    val nField: JValue = JObject(List(JField("N", JString("123"))))
    val aws: JValue = JObject(List(
      JField("nField", nField)))

    val expected: JValue = JObject(List(
      JField("nField", JDecimal(BigDecimal(123)))))

    AwsJsonReader.fromAws(aws) must be_==(expected)
  }

  def shrinkBoolField = {
    val boolField: JValue = JObject(List(JField("BOOL", JBool(true))))
    val aws: JValue = JObject(List(
      JField("boolField", boolField)))

    val expected: JValue = JObject(List(
      JField("boolField", JBool(true))))

    AwsJsonReader.fromAws(aws) must be_==(expected)
  }

  def shrinkSsField = {
    val ssField: JValue = JObject(List(JField("SS", JArray(List(JString("s1"), JString("s2"))))))

    val aws: JValue = JObject(List(
      JField("ssField", ssField)))

    val expected: JValue = JObject(List(
      JField("ssField", JArray(List(JString("s1"), JString("s2"))))))

    AwsJsonReader.fromAws(aws) must be_==(expected)
  }

  def shrinkLField = {
    val lField: JValue = JObject(List(JField("L", JArray(List(JInt(1), JInt(2))))))

    val aws: JValue = JObject(List(
      JField("lField", lField)))

    val expected: JValue = JObject(List(
      JField("lField", JArray(List(JInt(1), JInt(2))))))

    AwsJsonReader.fromAws(aws) must be_==(expected)
  }


  def shrinkMField = {
    val sField: JValue = JObject(List(JField("S", JString("s value"))))
    val mField: JValue = JObject(List(JField("M", JObject(List(JField("sField", sField))))))

    val aws: JValue = JObject(List(
      JField("mField", mField)))

    val expected: JValue = JObject(List(
      JField("mField", JObject(List(JField("sField", JString("s value")))))))

    AwsJsonReader.fromAws(aws) must be_==(expected)
  }

  def fromAws = {
    val sField: JValue = JObject(List(JField("S", JString("s value"))))
    val nField: JValue = JObject(List(JField("N", JString("123"))))
    val boolField: JValue = JObject(List(JField("BOOL", JBool(true))))
    val ssField: JValue = JObject(List(JField("SS", JArray(List(JString("s1"), JString("s2"))))))
    val lField: JValue = JObject(List(JField("L", JArray(List(JInt(1), JInt(2))))))
    val mField: JValue = JObject(List(JField("M", JObject(List(JField("sField", sField))))))

    val aws: JValue = JObject(List(
      JField("sField", sField),
      JField("nField", nField),
      JField("bField", boolField),
      JField("ssField", ssField),
      JField("lField", lField),
      JField("mField", mField)))

    val expected: JValue = JObject(List(
      JField("sField", JString("s value")),
      JField("nField", JDecimal(123)),
      JField("bField", JBool(true)),
      JField("ssField", JArray(List(JString("s1"), JString("s2")))),
      JField("lField", JArray(List(JInt(1), JInt(2)))),
      JField("mField", JObject(List(JField("sField", JString("s value")))))))

    AwsJsonReader.fromAws(aws) must be_==(expected)
  }
  
}
