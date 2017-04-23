package org.dynamite.action.delete

import dynamo.ast.S
import io.circe._
import org.dynamite.dsl.AwsTable
import org.specs2.mutable.Specification

class DeleteItemRequestTest extends Specification { override def is = s2"""
      Specifications for the companion object of DeleteItemRequest
        toJson should yield a correct Json $toJson
    """

  def toJson = {
    Encoder[DeleteItemRequest].apply(DeleteItemRequest(
      key = List("id" -> S("123")),
      table = AwsTable("test"))
    ) must be_==(
      Json.fromFields(List(
        "Key" -> Json.fromFields(List("id" -> Json.fromFields(List("S" -> Json.fromString("123"))))),
        "ReturnValues" -> Json.fromString("NONE"),
        "TableName" -> Json.fromString("test")
      ))
    )
  }
}
