package org.dynamite

import org.json4s.JsonAST._
import org.specs2.Specification

class AwsJsonWriterTest extends Specification { def is = s2"""
 Specification for the AwsJsonReader
   Json is augmented with Aws noise                 $toAws
  """

  def toAws = {
    val json = JObject(List(
      "sField" -> JString("s value")))

    val expected = JObject(List(
      "sField" -> JObject(List("S" -> JString("s value")))))

    Dummy.toAws(json) must be_==(expected)
  }


  private[this] object Dummy extends AwsJsonWriter

}
