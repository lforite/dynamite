package org.dynamite.ast

import org.scalacheck.Arbitrary._
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Gen._

object AwsTypesArbitraries {
  implicit val SArb: Arbitrary[S] = Arbitrary[S] { SGen }
  implicit val NArb: Arbitrary[N] = Arbitrary[N] { NGen }
  implicit val BOOLArb: Arbitrary[BOOL] = Arbitrary[BOOL] { BOOLGen }
  implicit val MArb: Arbitrary[M] = Arbitrary[M] { MGen }
  implicit val LArb: Arbitrary[L] = Arbitrary[L] { LGen }
  implicit val NSArb: Arbitrary[NS] = Arbitrary[NS] { NSGen }
  implicit val SSArb: Arbitrary[SS] = Arbitrary[SS] { SSGen }
  implicit val AwsTypeArb: Arbitrary[AwsType] = Arbitrary[AwsType] { AwsTypeGen }
  implicit val RootArb: Arbitrary[ROOT] = Arbitrary[ROOT] { ROOTGen }

  def AwsTypeGen: Gen[AwsType] = frequency(
    (5,
      delay(SimpleTypeGen)),
    (1, delay(oneOf(LGen, NSGen, SSGen, MGen, Gen.const(NULL))))
  )

  def SGen = arbitrary[String] map S
  def NGen = arbitrary[Number] map (i => N(i.toString))
  def BOOLGen = arbitrary[Boolean] map BOOL
  def MGen = listOfN(size, kvGen) map M
  def NSGen = listOfN(size, NGen) map (s => NS(s.toSet))
  def SSGen = listOfN(size, SGen) map (s => SS(s.toSet))

  def LGen: Gen[L] = listOfN(size, AwsTypeGen) map L
  def SimpleTypeGen: Gen[AwsScalarType] = oneOf(SGen, NGen, BOOLGen)

  def kvGen: Gen[(String, AwsType)] = for {
    key <- identifier
    value <- AwsTypeGen
  } yield (key, value)


  def ROOTGen: Gen[ROOT] = {
    listOfN(size, kvGen) map ROOT
  }

  private def size = choose(0, 5).sample.get
}
