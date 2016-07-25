package org.dynamite.dsl

import org.json4s.JsonDSL._
import org.json4s._

case class ConsumedCapacity(capacityUnits: Int,
  tableName: String
)

case class GetItemRequest(
  attributes: List[String] = List(),
  consistentRead: Boolean = false,
  expressionAttributeNames: Option[Map[String, String]] = None,
  key: Map[String, Map[String, String]],
  projection: Option[String] = None,
  returnConsumedCapacity: Option[String] = None,
  table: String
)

object GetItemRequest {
  def toJson(request: GetItemRequest): JValue = {
    ("Attributes" -> request.attributes) ~
      ("ConsistentRead" -> request.consistentRead) ~
      ("ExpressionAttributeNames" -> request.expressionAttributeNames) ~
      ("Key" -> request.key) ~
      ("ProjectionExpression" -> request.projection) ~
      ("ReturnConsumedCapacity" -> request.returnConsumedCapacity) ~
      ("TableName" -> request.table)
  }
}

case class GetItemResponse(
  item: JValue
)

object GetItemResponse {
  def fromJson(jValue: JValue): GetItemResponse = GetItemResponse(jValue \ "Item")
}