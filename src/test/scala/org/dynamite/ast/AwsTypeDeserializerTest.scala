package org.dynamite.ast

import org.json4s.JsonAST.{JArray, JBool, JObject, JString}
import org.specs2.{ScalaCheck, Specification}

class AwsTypeDeserializerTest extends Specification with ScalaCheck { override def is = s2"""
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

  import org.dynamite.dsl.Format.defaultFormats

  def sField = prop { str: String =>
    JObject("S" -> JString(str)).extract[AwsType] match {
      case S(value) => value must_== str
      case _ => false must_== true
    }
  }

  def nField = prop { number: Number =>
    JObject("N" -> JString(number.toString)).extract[AwsType] match {
      case N(value) => value must_== number.toString
      case _ => false must_== true
    }
  }

  def boolField = prop { bool: Boolean =>
    JObject("BOOL" -> JBool(bool)).extract[AwsType] match {
      case BOOL(value) => value must_== bool
      case _ => false must_== true
    }
  }

  def lField = prop { list: List[String] =>
    JObject("L" -> JArray(list.map(s => JObject("S" -> JString(s))))).extract[AwsType] match {
      case L(values) => values.length must_== list.size
      case _ => false must_== true
    }
  }

  def ssField = prop { ss: Set[String] =>
    JObject("SS" -> JArray(ss.map(s => JString(s)).toList)).extract[AwsType] match {
      case SS(values) => values.size must_== ss.size
      case _ => false must_== true
    }
  }

  def nsField = prop { sn: Set[Int] =>
    JObject("NS" -> JArray(sn.map(n => JString(n.toString)).toList)).extract[AwsType] match {
      case NS(values) => values.size must_== sn.size
      case _ => false must_== true
    }
  }

  def mField = prop { kvs: List[(String, String)] =>
    JObject("M" -> JObject(kvs.map(kv => (kv._1, JObject("S" -> JString(kv._2)))))).extract[AwsType] match {
      case M(elems) => elems.length must_== kvs.length
      case _ => false must_== true
    }
    true must_== true
  }

  def nullField = {
    JObject("NULL" -> JBool(true)).extract[AwsType] match {
      case NULL => success
      case _ => failure
    }
  }

  def rootField = {
    JObject("test" -> JObject("N" -> JString("-1"))).extract[AwsType] match {
      case ROOT(elems) => elems.size must_== 1
      case _ => failure
    }
  }

}
