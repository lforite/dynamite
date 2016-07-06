package org.dynamite

import org.json4s.JsonAST.{JField, JObject, JString, JValue}
import org.specs2._

class AwsJsonParserTest extends Specification { def is = s2"""
 Specification for the AwsJsonParser
   Aws noise is swallowed by the parser                 $fromAws
  """

  def fromAws = {
    val sField: JValue = JObject(List(JField("S", JString("s value"))))
    val nField: JValue = JObject(List(JField("N", JString("n value"))))
    val aws: JValue = JObject(List(JField("sField", sField), JField("nField", nField)))
    val expected: JValue = JObject(List(JField("sField", JString("s value")), JField("nField", JString("n value"))))

    Dummy.fromAws(aws) must be_==(expected)
  }


  private[this] object Dummy extends AwsJsonParser
}
