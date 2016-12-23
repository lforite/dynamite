package org.dynamite.action.put

import dynamo.ast.DynamoType
import org.dynamite.ast.ROOT
import org.dynamite.dsl._
import org.json4s.Extraction._
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import scalaz.Scalaz._
import scalaz.\/

private[dynamite] case class PutItemRequest[A](
  item: DynamoType,
  conditionExpression: Option[String] = None,
  expressionAttributeNames: Option[Map[String, String]] = None,
  expressionAttributeValues: Option[Map[String, String]] = None,
  returnConsumedCapacity: Option[String] = None,
  table: AwsTable,
  returnItemCollectionMetrics: Option[String] = None,
  returnValues: Option[String] = None
)

private[dynamite] object PutItemRequest {
  implicit def toRequestBody[A] = new JsonSerializable[PutItemRequest[A]] {
    def serialize(putItemRequest: PutItemRequest[A]): DynamoCommonError \/ RequestBody = {
      (for {
        json <- toJson(putItemRequest)
        renderedJson <- render(json)(Format.defaultFormats).right
        body <- \/.fromTryCatchNonFatal[String](compact(renderedJson))
      } yield RequestBody(body)) leftMap (e => JsonSerialisationError)
    }
  }

  private def toJson[A](request: PutItemRequest[A]): DynamoCommonError \/ JValue = {
    (for {
      item <- \/.fromTryCatchNonFatal[JValue](decompose(ROOT(request.item))(Format.defaultFormats))
    } yield {
      ("Item" -> item) ~
        ("ConditionExpression" -> request.conditionExpression) ~
        ("ExpressionAttributeNames" -> request.expressionAttributeNames) ~
        ("ExpressionAttributeValues" -> request.expressionAttributeValues) ~
        ("ReturnConsumedCapacity" -> request.returnConsumedCapacity) ~
        ("TableName" -> request.table.value) ~
        ("ReturnItemCollectionMetrics" -> request.returnItemCollectionMetrics) ~
        ("ReturnValue" -> request.returnValues)
    }) leftMap (e => JsonSerialisationError)
  }
}
