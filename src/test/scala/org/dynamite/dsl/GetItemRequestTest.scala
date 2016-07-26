package org.dynamite.dsl

import org.dynamite.ast.{AwsTypeSerializer, S}
import org.json4s.JsonAST.JObject
import org.json4s._
import org.specs2.mutable.Specification

class GetItemRequestTest extends Specification { override def is = s2"""
      Specifications for the companion object of GetItemRequest
        toJson should yield a correct Json $toJson
    """

  def toJson = {
    GetItemRequest.toJson(GetItemRequest(
      key = List("id" -> S("123")),
      table = "test")
    )(DefaultFormats + new AwsTypeSerializer) must be_==(
      JObject(
        "Attributes" -> JArray(Nil),
        "ConsistentRead" -> JBool(false),
        "ExpressionAttributeNames" -> JNothing,
        "Key" -> JObject("id" -> JObject("S" -> JString("123"))),
        "ProjectionExpression" -> JNothing,
        "ReturnConsumedCapacity" -> JNothing,
        "TableName" -> JString("test")
      )
    )
  }
}
