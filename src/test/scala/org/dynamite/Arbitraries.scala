package org.dynamite

import org.json4s.JsonAST._
import org.scalacheck.Gen.{listOf, const}
import org.scalacheck.{Arbitrary, Gen}
import Arbitrary.arbitrary

object Arbitraries {

  implicit val jsObjectArbitrary = Arbitrary {
    for {
      fields <- listOf(fieldArbitrary)
    } yield JObject(fields)
  }

  def fieldArbitrary: Gen[JField] =
    for {
      field <- arbitrary[String]
      value <- jsValueGen
    } yield (field, value)

  def jsValueGen = arbitrary[String].map(JString(_))
}
