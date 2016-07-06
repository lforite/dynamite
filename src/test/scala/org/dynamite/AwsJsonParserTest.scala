package org.dynamite

import org.json4s.JsonAST.{JField, JObject, JString, JValue}
import org.specs2._

class AwsJsonParserTest extends Specification { def is = s2"""
 Specification for the AwsJsonParser
   it is working                 $fromAws
  """

  def fromAws = {
    val sField: JValue = JObject(List(JField("S", JString("inner value"))))
    val aws: JValue = JObject(List(JField("inner", sField)))
    val expected: JValue = JObject(List(JField("inner", JString("inner value"))))

    Dummy.fromAws(aws) must be_==(expected)
  }


  private[this] object Dummy extends AwsJsonParser
}
