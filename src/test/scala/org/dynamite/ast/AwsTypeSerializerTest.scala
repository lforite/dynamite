package org.dynamite.ast

import dynamo.ast._
import io.circe._
import org.dynamite.ast.AwsTypeSerialiser._
import org.dynamite.ast.AwsTypesArbitraries._
import org.specs2.{ScalaCheck, Specification}

class AwsTypeSerializerTest extends Specification with ScalaCheck { override def is = s2"""
 Specification for the AwsJsonReader
   S fields are correctly serialised $sField
   N fields are correctly serialised $nField
   BOOL fields are correctly serialised $boolField
   L fields are correctly serialised $lField
   M fields are correctly serialised $mField
   NS fields are correctly serialised $nsField
   SS fields are correctly serialised $ssField
  """

  def sField = prop { s: S =>
    Encoder[S].apply(s) must_== Json.fromFields(List("S" -> Json.fromString(s.value)))
  }

  def nField = prop { n: N =>
    Encoder[N].apply(n) must_== Json.fromFields(List("N" -> Json.fromString(n.value)))
  }

  def boolField = prop { bool: BOOL =>
    Encoder[BOOL].apply(bool) must_== Json.fromFields(List("BOOL" -> Json.fromBoolean(bool.value)))
  }

  def lField = prop { l: L[DynamoType] =>
    val result = AwsTypeSerialiser.encodeL.apply(l)

    result.isObject must_== true
    result.asObject.get.fields.head must_== "L"
    result.asObject.get.values.head.isArray must_== true
    result.asObject.get.values.head.asArray.get.length must_== l.elements.length
  }

  def mField = prop { m: M =>
    val result = Encoder[M].apply(m)

    result.isObject must_== true
    result.asObject.get.toList.head._1 must_== "M"
    result.asObject.get.toList.head._2.isObject must_== true
    result.asObject.get.toList.head._2.asObject.get.toList.length must_== m.elements.length
  }

  def nsField = prop { ns: NS =>
    val result = Encoder[NS].apply(ns)

    result.isObject must_== true
    result.asObject.get.toList.head._1 must_== "NS"
    result.asObject.get.toList.head._2.isArray must_== true
    result.asObject.get.toList.head._2.asArray.get.length must_== ns.numbers.size
  }

  def ssField = prop { ss: SS =>
    val result = Encoder[SS].apply(ss)

    result.isObject must_== true
    result.asObject.get.toList.head._1 must_== "SS"
    result.asObject.get.toList.head._2.isArray must_== true
    result.asObject.get.toList.head._2.asArray.get.length must_== ss.strings.size
  }

  def nullField = {
    Encoder[NULL.type].apply(NULL) must_== Json.fromFields(List("NULL" -> Json.fromBoolean(true)))
  }
}
