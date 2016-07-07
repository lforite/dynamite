package org.dynamite

import org.json4s.JsonAST._
import org.specs2._

class AwsJsonReaderTest extends Specification { def is = s2"""
 Specification for the AwsJsonReader
   Aws noise is swallowed by the parser                 $fromAws
  """

  def fromAws = {
    val sField: JValue = JObject(List(JField("S", JString("s value"))))
    val nField: JValue = JObject(List(JField("N", JString("n value"))))
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
      JField("nField", JString("n value")),
      JField("bField", JBool(true)),
      JField("ssField", JArray(List(JString("s1"), JString("s2")))),
      JField("lField", JArray(List(JInt(1), JInt(2)))),
      JField("mField", JObject(List(JField("sField", JString("s value")))))))

    Dummy.fromAws(aws) must be_==(expected)
  }


  private[this] object Dummy extends AwsJsonReader

}
