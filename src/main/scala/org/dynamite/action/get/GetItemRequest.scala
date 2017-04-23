package org.dynamite.action.get

import dynamo.ast.DynamoScalarType
import io.circe.{Printer, _}
import io.circe.syntax._
import org.dynamite.ast.AwsTypeSerialiser._
import org.dynamite.dsl._

import scalaz.\/

private[dynamite] case class GetItemRequest(
  attributes: Option[List[String]] = None,
  consistentRead: Boolean = false,
  expressionAttributeNames: Option[Map[String, String]] = None,
  key: List[(String, DynamoScalarType)],
  projection: Option[String] = None,
  returnConsumedCapacity: Option[String] = None,
  table: AwsTable)

private[dynamite] object GetItemRequest {
  val printer: Printer = Printer.noSpaces.copy(dropNullKeys = true)

  implicit val GetItemRequestEncoder: Encoder[GetItemRequest] = Encoder.forProduct7(
    "Attributes",
    "ConsistentRead",
    "ExpressionAttributeNames",
    "Key",
    "ProjectionExpression",
    "ReturnConsumedCapacity",
    "TableName"
  )((request: GetItemRequest) =>
    (request.attributes,
      request.consistentRead,
      request.expressionAttributeNames,
      request.key.toMap,
      request.projection,
      request.returnConsumedCapacity,
      request.table.value))

  implicit val toRequestBody = new JsonSerializable[GetItemRequest] {
    def serialize(getItemRequest: GetItemRequest): DynamoCommonError \/ RequestBody = {
      (for {
        body <- \/.fromTryCatchNonFatal[String](printer.pretty(getItemRequest.asJson))
      } yield RequestBody(body)) leftMap (e => JsonSerialisationError)
    }
  }

}
