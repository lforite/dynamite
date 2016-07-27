package org.dynamite.dsl

import org.dynamite.ast.AwsScalarType
import org.json4s.Extraction.decompose
import org.json4s.JsonDSL._
import org.json4s._

case class ConsumedCapacity(capacityUnits: Int,
  tableName: String
)

case class GetItemRequest(
  attributes: List[String] = List(),
  consistentRead: Boolean = false,
  expressionAttributeNames: Option[Map[String, String]] = None,
  key: List[(String, AwsScalarType)],
  projection: Option[String] = None,
  returnConsumedCapacity: Option[String] = None,
  table: AwsTable)

object GetItemRequest {
  def toJson(request: GetItemRequest)(implicit formats: Formats): JValue = {
    ("Attributes" -> request.attributes) ~
      ("ConsistentRead" -> request.consistentRead) ~
      ("ExpressionAttributeNames" -> request.expressionAttributeNames) ~
      ("Key" -> request.key.map(k => (k._1, decompose(k._2)))) ~
      ("ProjectionExpression" -> request.projection) ~
      ("ReturnConsumedCapacity" -> request.returnConsumedCapacity) ~
      ("TableName" -> request.table.value)
  }
}

case class GetItemResponse(
  item: JValue
)

object GetItemResponse {
  def fromJson(jValue: JValue): GetItemResponse = GetItemResponse(jValue \ "Item")
}