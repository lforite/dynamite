package org.dynamite

import org.dynamite.dsl.StatusCode
import org.json4s.DefaultFormats
import org.json4s.JsonAST.{JObject, _}
import org.json4s.jackson.Serialization.write
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{oneOf, _}
import org.scalacheck.{Arbitrary, Gen}

/** Encapsulate a valid json as a string */
case class ValidJson(json: String)

object Arbitraries {

  implicit private val formats = DefaultFormats

  implicit val validJsonArbitrary = Arbitrary[ValidJson] {
    for {
      jsObject <- genJObject
    } yield ValidJson(write(jsObject))
  }

  implicit val jsObjectArbitrary = Arbitrary[JObject] {
    genJObject
  }

  implicit val statusCodesArbitrary = Arbitrary[StatusCode] {
    Gen.choose(200, 500) map StatusCode
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
      arbitrary[String].map(JString),
      arbitrary[Int].map(JInt(_)),
      arbitrary[Double].map(JDecimal(_)),
      arbitrary[Long].map(JLong),
      arbitrary[Boolean].map(JBool(_)))
  }

  private def size = choose(0, 3).sample.get
}
