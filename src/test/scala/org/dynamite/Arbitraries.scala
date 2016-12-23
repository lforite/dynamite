package org.dynamite

import org.dynamite.dsl.StatusCode
import org.dynamite.http.HttpHeader
import org.json4s.JsonAST.{JArray, JObject, JString, _}
import org.json4s.jackson.JsonMethods._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{oneOf, _}
import org.scalacheck.{Arbitrary, Gen}
import org.dynamite.dsl.Format._

/** Encapsulate a valid json as a string */
case class ValidJson(json: String)

object Arbitraries {

  implicit val validJsonArbitrary: Arbitrary[ValidJson] = Arbitrary[ValidJson] {
    for {
      jsObject <- genJObject
    } yield ValidJson(compact(render(jsObject)))
  }

  implicit val jsObjectArbitrary: Arbitrary[JObject] = Arbitrary[JObject] {
    genJObject
  }

  implicit val statusCodesArbitrary: Arbitrary[StatusCode] = Arbitrary[StatusCode] {
    Gen.choose(200, 500) map StatusCode
  }

  implicit val headersArbitrary: Arbitrary[List[HttpHeader]] = Arbitrary[List[HttpHeader]] {
    listOfN(choose(0, 30).sample.get, genHeader)
  }

  private def genHeader: Gen[HttpHeader] = for {
    id <- identifier
    anyString <- alphaStr.filter(_.trim.length > 0)
  } yield new HttpHeader {
    def render: (String, String) = id -> anyString
  }

  private def genJObject: Gen[JObject] = for {
    fields <- listOfN(5, genField)
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

  private def size = choose(1, 3).sample.get
}
