package org.dynamite

import org.json4s.JsonAST.{JObject, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{oneOf, _}
import org.scalacheck.{Arbitrary, Gen}

object Arbitraries {

  implicit val jsObjectArbitrary = Arbitrary[JObject] {
    genJObject
  }

  private def genJObject: Gen[JObject] = for {
    fields <- listOfN(size, genField)
  } yield JObject(fields)

  private def genField: Gen[JField] = for {
    field <- identifier
    value <- genValue
  } yield (field, value)

  private def genValue: Gen[JValue] = frequency(
    (10,
      delay(genSimpleValue)),
    (1,
      listOfN(size, arbitrary[String]).map(js => JArray(js map JString))),
    (1,
      delay(Gen.containerOfN[List, JValue](size, arbitrary[JObject]).map(JArray))))

  def genSimpleValue: Gen[JValue] = {
    oneOf(
      arbitrary[String].map(JString(_)),
      arbitrary[Int].map(JInt(_)),
      arbitrary[Double].map(JDecimal(_)),
      arbitrary[Long].map(JLong(_)),
      arbitrary[Boolean].map(JBool(_)))
  }

  private def size = choose(0, 5).sample.get
}
