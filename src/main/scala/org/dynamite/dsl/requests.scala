package org.dynamite.dsl

import org.dynamite.ast.{AwsScalarType, AwsTypeSerializer}
import org.json4s.Extraction.decompose
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scalaz.Scalaz._
import scalaz.\/

trait JsonSerializable[A] {
  def serialize(a: A): DynamoError \/ RequestBody
}

object JsonSerializable {
  def apply[A](implicit ev: JsonSerializable[A]) = ev
}

trait JsonDeserializable[A] {
  def deserialize(jValue: JValue): A
}

object JsonDeserializable {
  def apply[A](implicit ev: JsonDeserializable[A]) = ev
}

case class ConsumedCapacity(capacityUnits: Int, tableName: String)

case class GetItemRequest(
  attributes: List[String] = List(),
  consistentRead: Boolean = false,
  expressionAttributeNames: Option[Map[String, String]] = None,
  key: List[(String, AwsScalarType)],
  projection: Option[String] = None,
  returnConsumedCapacity: Option[String] = None,
  table: AwsTable)

object GetItemRequest {
  implicit private val formats = DefaultFormats + new AwsTypeSerializer

  implicit val getItemRequestTypeClass = new JsonSerializable[GetItemRequest] {
    def serialize(getItemRequest: GetItemRequest): DynamoError \/ RequestBody = {
      (for {
        json <- GetItemRequest.toJson(getItemRequest).right
        renderedJson <- render(json).right
        body <- \/.fromTryCatchThrowable[String, Throwable](compact(renderedJson))
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

case class GetItemResponse(item: JValue)

object GetItemResponse {
  implicit val fromJson = new JsonDeserializable[GetItemResponse] {
    override def deserialize(jValue: JValue): GetItemResponse =
      GetItemResponse(jValue \ "Item")
  }
}

case class GetItemResult[A](item: Option[A])
