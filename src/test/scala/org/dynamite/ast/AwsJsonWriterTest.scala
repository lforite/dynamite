package org.dynamite.ast

import org.json4s.JsonAST.{JArray, _}

class AwsJsonWriterTest extends org.specs2.mutable.Specification {

  lazy val stringCase = ("S", "sField", JString("s value"), JString("s value"))
  lazy val intCase = ("N", "nField", JInt(1), JString("1"))
  lazy val decimalCase = ("N", "nField", JDecimal(12.0), JString("12.0"))
  lazy val longCase = ("N", "nField", JLong(9L), JString("9"))
  lazy val boolCase = ("BOOL", "bField", JBool(true), JBool(true))
  lazy val arrayCase = ("SS", "aField", JArray(List(JString("a"), JString("b"))), JArray(List(JString("a"), JString("b"))))
  lazy val objectCase = ("M", "mField", JObject(List("inner" -> JString("hello !"))), JObject(List("inner" -> JObject(List("S" -> JString("hello !"))))))
  lazy val arrayObjectCase = ("L", "oField", JArray(List(JObject(List("inner" -> JString("hello !"))))), JArray(List(JObject(List("M" -> JObject(List("inner" -> JObject(List("S" -> JString("hello !"))))))))))

  Seq(
    stringCase,
    intCase,
    decimalCase,
    longCase,
    boolCase,
    arrayCase,
    objectCase,
    arrayObjectCase) foreach { testCase =>
    s"Field ${testCase._1} is correctly represented" >> {
      val json = JObject(List(
        testCase._2 -> testCase._3))

      val expected = JObject(List(
        testCase._2 -> JObject(List(testCase._1 -> testCase._4))))

      AwsJsonWriter.toAws(json) must be_==(expected)
    }
  }

}
