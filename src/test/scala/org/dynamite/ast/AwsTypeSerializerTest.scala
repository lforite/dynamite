package org.dynamite.ast

import dynamo.ast._
import org.dynamite.ast.AwsTypesArbitraries._
import org.json4s.Extraction._
import org.json4s.JsonAST._
import org.specs2._

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

  import org.dynamite.dsl.Format.defaultFormats

  def sField = prop { s: S =>
    decompose(s) must_== JObject("S" -> JString(s.value))
  }

  def nField = prop { n: N =>
    decompose(n) must_== JObject("N" -> JString(n.value))
  }

  def boolField = prop { bool: BOOL =>
    decompose(bool) must_== JObject("BOOL" -> JBool(bool.value))
  }

  def lField = prop { l: L[DynamoType] =>
    val decomposed = decompose(l)
    decomposed.isInstanceOf[JObject] must_== true
    decomposed.asInstanceOf[JObject].obj.head._1 must_== "L"
    decomposed.asInstanceOf[JObject].obj.head._2.isInstanceOf[JArray] must_== true
    decomposed.asInstanceOf[JObject].obj.head._2.asInstanceOf[JArray].arr.length must_== l.elements.length
  }

  def mField = prop { m: M =>
    val decomposed = decompose(m)
    decomposed.isInstanceOf[JObject] must_== true
    decomposed.asInstanceOf[JObject].obj.head._1 must_== "M"
    decomposed.asInstanceOf[JObject].obj.head._2.isInstanceOf[JObject] must_== true
    decomposed.asInstanceOf[JObject].obj.head._2.asInstanceOf[JObject].obj.length must_== m.elements.length
  }

  def nsField = prop { ns: NS =>
    val decomposed = decompose(ns)
    decomposed.isInstanceOf[JObject] must_== true
    decomposed.asInstanceOf[JObject].obj.head._1 must_== "NS"
    decomposed.asInstanceOf[JObject].obj.head._2.isInstanceOf[JArray] must_== true
    decomposed.asInstanceOf[JObject].obj.head._2.asInstanceOf[JArray].arr.length must_== ns.numbers.size
  }

  def ssField = prop { ss: SS =>
    val decomposed = decompose(ss)
    decomposed.isInstanceOf[JObject] must_== true
    decomposed.asInstanceOf[JObject].obj.head._1 must_== "SS"
    decomposed.asInstanceOf[JObject].obj.head._2.isInstanceOf[JArray] must_== true
    decomposed.asInstanceOf[JObject].obj.head._2.asInstanceOf[JArray].arr.length must_== ss.strings.size
  }

  def nullField = {
    decompose(BOOL) must_== JObject("NULL" -> JBool(true))
  }
}
