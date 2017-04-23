package org.dynamite.ast

import dynamo.ast._
import org.scalacheck.Arbitrary._
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Gen._

object AwsTypesArbitraries {
  implicit val SArb: Arbitrary[S] = Arbitrary[S] { SGen }
  implicit val NArb: Arbitrary[N] = Arbitrary[N] { NGen }
  implicit val BOOLArb: Arbitrary[BOOL] = Arbitrary[BOOL] { BOOLGen }
  implicit val MArb: Arbitrary[M] = Arbitrary[M] { MGen }
  implicit val LArb: Arbitrary[L[DynamoType]] = Arbitrary[L[DynamoType]] { LGen }
  implicit val NSArb: Arbitrary[NS] = Arbitrary[NS] { NSGen }
  implicit val SSArb: Arbitrary[SS] = Arbitrary[SS] { SSGen }
  implicit val AwsTypeArb: Arbitrary[DynamoType] = Arbitrary[DynamoType] { AwsTypeGen }

  def AwsTypeGen: Gen[DynamoType] = frequency(
    (5,
      delay(SimpleTypeGen)),
    (1, delay(oneOf(LGen, NSGen, SSGen, MGen, Gen.const(NULL))))
  )

  def SGen: Gen[S] = arbitrary[String] map S
  def NGen: Gen[N] = arbitrary[Int] map (i => N(i.toString))
  def BOOLGen: Gen[BOOL] = arbitrary[Boolean] map BOOL
  def MGen: Gen[M] = listOfN(size, kvGen).map(_.toMap).map(_.toList) map M
  def NSGen: Gen[NS] = listOfN(size, NGen) map (s => NS(s.toSet))
  def SSGen: Gen[SS] = listOfN(size, SGen) map (s => SS(s.toSet))

  def LGen: Gen[L[DynamoType]] = listOfN(size, AwsTypeGen) map (L(_))
  def SimpleTypeGen: Gen[DynamoScalarType] = oneOf(SGen, NGen, BOOLGen)

  def kvGen: Gen[(String, DynamoType)] = for {
    key <- identifier
    value <- AwsTypeGen
  } yield (key, value)


  private def size = choose(0, 3).sample.get
}
