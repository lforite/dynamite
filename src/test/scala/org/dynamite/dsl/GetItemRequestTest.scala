package org.dynamite.dsl

import dynamo.ast.S
import org.dynamite.action.get.GetItemRequest
import org.dynamite.dsl.Format._
import org.json4s.JsonAST.{JBool, JNothing, JObject, JString}
import org.specs2.mutable.Specification

class GetItemRequestTest extends Specification { override def is = s2"""
      Specifications for the companion object of GetItemRequest
        toJson should yield a correct Json $toJson
    """

  def toJson = {
    GetItemRequest.toJson(GetItemRequest(
      key = List("id" -> S("123")),
      table = AwsTable("test"))
    ) must be_==(
      JObject(
        "Attributes" -> JNothing,
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
