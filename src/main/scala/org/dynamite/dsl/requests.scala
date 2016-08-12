package org.dynamite.dsl

import org.dynamite.ast.{AwsScalarType, AwsTypeSerializer}
import org.dynamite.http.{AcceptEncodingHeader, AmazonTargetHeader, ContentTypeHeader, HttpHeader}
import org.json4s.Extraction.decompose
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scalaz.Scalaz._
import scalaz.\/

case class ConsumedCapacity(capacityUnits: Int,
  tableName: String
)


trait AwsOperation[REQUEST, RESPONSE, RESULT]



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

  implicit val getItemRequestTypeClass = new JsonSerializable[GetItemRequest] with HasHeader[GetItemRequest] {
    def serialize(getItemRequest: GetItemRequest): DynamoError \/ RequestBody = {
      (for {
        json <- GetItemRequest.toJson(getItemRequest).right
        renderedJson <- render(json).right
        body <- \/.fromTryCatchThrowable[String, Throwable](compact(renderedJson))
      } yield RequestBody(body)) leftMap (e => JsonSerialisationError)
    }

    override def headers(): List[HttpHeader] = List(
      AcceptEncodingHeader("identity"),
      ContentTypeHeader("application/x-amz-json-1.0"),
      AmazonTargetHeader("DynamoDB_20120810.GetItem"))
  }

  //move this in the method call
  val headers = List(
    AcceptEncodingHeader("identity"),
    ContentTypeHeader("application/x-amz-json-1.0"),
    AmazonTargetHeader("DynamoDB_20120810.GetItem"))

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
  implicit val fromJson = new FromJson[GetItemResponse] {
    override def fromJson(jValue: JValue): GetItemResponse =
      GetItemResponse(jValue \ "Item")
  }
}

case class GetItemResult[A](item: Option[A])

trait JsonSerializable[A] {
  def serialize(a: A): DynamoError \/ RequestBody
}
object JsonSerializable {
  //implicit class JsonSerializablePostFix
  def apply[A](implicit ev:JsonSerializable[A]) = ev
}

trait HasHeader[A] {
  def headers(): List[HttpHeader]
}

trait FromJson[A] {
  def fromJson(jValue: JValue): A
}

trait ToResult[A, B] {
  def toResult(a: A): DynamoError \/ B
}
