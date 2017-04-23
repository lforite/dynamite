package org.dynamite.ast

import dynamo.ast._
import io.circe._
import org.dynamite.ast.AwsTypeSerialiser._
import org.specs2.{ScalaCheck, Specification}

import scala.util.Right

class AwsTypeDeserializerTest extends Specification with ScalaCheck {
  override def is = s2"""
 Specification for the AwsJsonReader
   S fields are correctly deserialised $sField
   N fields are correctly deserialised $nField
   BOOL fields are correctly deserialised $boolField
   L fields are correctly deserialised $lField
   SS fields are correctly deserialised $ssField
   NS fields are correctly deserialised $nsField
   M fields are correctly deserialised $mField
   NULL fields are correctly deserialised $nullField
  """

  def sField = prop { str: String =>
    val json = Json.fromFields(List("S" -> Json.fromString(str)))
    Decoder[DynamoType].decodeJson(json) must_== Right(S(str))
  }

  def nField = prop { number: Number =>
    val json = Json.fromFields(List("N" -> Json.fromString(number.toString)))
    Decoder[DynamoType].decodeJson(json) must_== Right(N(number.toString))
  }

  def boolField = prop { bool: Boolean =>
    val json = Json.fromFields(List("BOOL" -> Json.fromBoolean(bool)))
    Decoder[DynamoType].decodeJson(json) must_== Right(BOOL(bool))
  }

  def lField = prop { list: List[String] =>
    val jsArray = Json.arr(list.map(s => Json.fromFields(List("N" -> Json.fromString(s)))): _*)
    val json = Json.fromFields(List("L" -> jsArray))
    Decoder[DynamoType].decodeJson(json) match {
      case Right(L(v)) => v.length must_== list.size
      case e => ko(s"L was expected to be correctly deserialised, got: $e instead")
    }
  }

  def ssField = prop { ss: Set[String] =>
    val jsArray = Json.arr(ss.map(Json.fromString).toSeq: _*)
    val json = Json.fromFields(List("SS" -> jsArray))
    Decoder[DynamoType].decodeJson(json) match {
      case Right(SS(values)) => values.map(_.value) must_== ss
      case e => ko(s"SS was expected to be correctly deserialised, got: $e instead")
    }
  }

  def nsField = prop { ss: Set[Int] =>
    val jsArray = Json.arr(ss.map(int => Json.fromString(int.toString)).toSeq: _*)
    val json = Json.fromFields(List("NS" -> jsArray))
    Decoder[DynamoType].decodeJson(json) match {
      case Right(NS(values)) => values.map(_.value.toInt) must_== ss
      case e => ko(s"NS was expected to be correctly deserialised, got: $e instead")
    }
  }

  def mField = prop { kvs: Map[String, String] =>
    val values = kvs.map(kv => (kv._1, Json.fromFields(List("S" -> Json.fromString(kv._2))))).toIterable
    val json = Json.fromFields(List("M" -> Json.fromFields(values)))
    Decoder[DynamoType].decodeJson(json) match {
      case Right(M(elems)) => elems.length must_== values.size
      case e => ko(s"M was expected to be correctly deserialised, got: $e instead")
    }
  }

  def nullField = {
    val json = Json.fromFields(List("NULL" -> Json.fromBoolean(true)))
    Decoder[DynamoType].decodeJson(json) match {
      case Right(NULL) => ok
      case e => ko(s"NULL was expected to be correctly deserialised, got: $e instead")
    }
  }
}
