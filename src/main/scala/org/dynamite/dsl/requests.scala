package org.dynamite.dsl

import org.dynamite.ast.{AwsScalarType, AwsTypeSerializer}
import org.json4s.Extraction.decompose
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scalaz.Scalaz._
import scalaz.\/

private[dynamite] trait DynamoProtocol[REQUEST, RESPONSE, RESULT]

private[dynamite] object DynamoProtocol {
  implicit def GetItemProtocol[A] = new DynamoProtocol[GetItemRequest, GetItemResponse, GetItemResult[A]] {}
}

private[dynamite] trait JsonSerializable[A] {
  def serialize(a: A): DynamoError \/ RequestBody
}

private[dynamite] object JsonSerializable {
  def apply[A](implicit ev: JsonSerializable[A]) = ev
}

private[dynamite] trait JsonDeserializable[A] {
  def deserialize(jValue: JValue): A
}

private[dynamite] object JsonDeserializable {
  def apply[A](implicit ev: JsonDeserializable[A]) = ev
}

private[dynamite] case class GetItemRequest(
  attributes: List[String] = List(),
  consistentRead: Boolean = false,
  expressionAttributeNames: Option[Map[String, String]] = None,
  key: List[(String, AwsScalarType)],
  projection: Option[String] = None,
  returnConsumedCapacity: Option[String] = None,
  table: AwsTable)

private[dynamite] object GetItemRequest {
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

private[dynamite] case class GetItemResponse(item: JValue)

private[dynamite] object GetItemResponse {
  implicit val fromJson = new JsonDeserializable[GetItemResponse] {
    override def deserialize(jValue: JValue): GetItemResponse =
      GetItemResponse(jValue \ "Item")
  }
}
