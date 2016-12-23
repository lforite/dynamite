package org.dynamite.action.delete

import dynamo.ast.S
import org.dynamite.dsl.AwsTable
import org.json4s.JsonAST.{JObject, JString}
import org.specs2.mutable.Specification
import org.dynamite.dsl.Format._

class DeleteItemRequestTest extends Specification { override def is = s2"""
      Specifications for the companion object of DeleteItemRequest
        toJson should yield a correct Json $toJson
    """

  def toJson = {
    DeleteItemRequest.toJson(DeleteItemRequest(
      key = List("id" -> S("123")),
      table = AwsTable("test"))
    ) must be_==(
      JObject(
        "Key" -> JObject("id" -> JObject("S" -> JString("123"))),
        "ReturnValues" -> JString("NONE"),
        "TableName" -> JString("test")
      )
    )
  }
}
