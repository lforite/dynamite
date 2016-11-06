package org.dynamite.action.get

import org.dynamite.ast.{AwsScalarType, AwsTypeSerializer}
import org.dynamite.dsl._
import org.json4s.Extraction._
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats, _}
import org.json4s.JsonDSL._
import org.json4s._

import scalaz.Scalaz._
import scalaz.\/

private[dynamite] case class GetItemRequest(
  attributes: Option[List[String]] = None,
  consistentRead: Boolean = false,
  expressionAttributeNames: Option[Map[String, String]] = None,
  key: List[(String, AwsScalarType)],
  projection: Option[String] = None,
  returnConsumedCapacity: Option[String] = None,
  table: AwsTable)

private[dynamite] object GetItemRequest {
  implicit private val formats = DefaultFormats + new AwsTypeSerializer

  implicit val toRequestBody = new JsonSerializable[GetItemRequest] {
    def serialize(getItemRequest: GetItemRequest): DynamoCommonError \/ RequestBody = {
      (for {
        json <- toJson(getItemRequest).right
        renderedJson <- render(json).right
        body <- \/.fromTryCatchNonFatal[String](compact(renderedJson))
      } yield RequestBody(body)) leftMap (e => JsonSerialisationError)
    }
  }

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