package org.dynamite.action.get

import dynamo.ast.S
import io.circe.{Encoder, Json}
import org.dynamite.dsl.AwsTable
import org.specs2.mutable.Specification

class GetItemRequestTest extends Specification { override def is = s2"""
      Specifications for the companion object of GetItemRequest
        toJson should yield a correct Json $toJson
    """

  def toJson = {
    Encoder[GetItemRequest].apply(GetItemRequest(
      key = List("id" -> S("123")),
      table = AwsTable("test"))
    ) must be_==(
      Json.fromFields(List(
        "Attributes" -> Json.Null,
        "ConsistentRead" -> Json.False,
        "ExpressionAttributeNames" -> Json.Null,
        "Key" -> Json.fromFields(List("id" -> Json.fromFields(List("S" -> Json.fromString("123"))))),
        "ProjectionExpression" -> Json.Null,
        "ReturnConsumedCapacity" -> Json.Null,
        "TableName" -> Json.fromString("test")
      ))
    )
  }
}
