package org.dynamite

import io.circe.Json
import io.circe.syntax._
import org.dynamite.dsl.StatusCode
import org.dynamite.http.HttpHeader
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{oneOf, _}
import org.scalacheck.{Arbitrary, Gen}

/** Encapsulate a valid json as a string */
case class ValidJson(json: String)

object Arbitraries {

  implicit val validJsonArbitrary: Arbitrary[ValidJson] = Arbitrary[ValidJson] {
    for {
      jsObject <- genJObject
    } yield ValidJson(jsObject.asJson.noSpaces)
  }

  implicit val jsObjectArbitrary: Arbitrary[Json] = Arbitrary[Json] {
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

  private def genJObject: Gen[Json] = for {
    fields <- listOfN(5, genField)
  } yield Json.fromFields(fields)

  private def genField: Gen[(String, Json)] = for {
    field <- identifier
    value <- genValue
  } yield (field, value)

  private def genValue: Gen[Json] = frequency(
    (10,
      delay(genSimpleValue)),
    (1,
      listOfN(size, arbitrary[String]).map(js => Json.fromValues(js map Json.fromString))),
    (1,
      delay(Gen.containerOfN[List, Json](size, arbitrary[Json]).map(Json.fromValues))))

  def genSimpleValue: Gen[Json] = {
    oneOf(
      arbitrary[String].map(Json.fromString),
      arbitrary[Int].map(Json.fromInt),
      arbitrary[Double].map(Json.fromDouble(_).get),
      arbitrary[Long].map(Json.fromLong),
      arbitrary[Boolean].map(Json.fromBoolean))
  }

  private def size = choose(1, 3).sample.get
}
