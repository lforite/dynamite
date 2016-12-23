package org.dynamite.action.delete

import dynamo.ast.DynamoScalarType
import org.dynamite.dsl._
import org.json4s.Extraction._
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scalaz.Scalaz._
import scalaz.\/

private[dynamite] case class DeleteItemRequest(
  key: List[(String, DynamoScalarType)],
  returnValues: Option[String] = Some("NONE"),
  table: AwsTable
)


private[dynamite] object DeleteItemRequest {

  implicit val toRequestBody = new JsonSerializable[DeleteItemRequest] {
    def serialize(request: DeleteItemRequest): DynamoCommonError \/ RequestBody = {
      import org.dynamite.dsl.Format._
      (for {
        json <- toJson(request).right
        renderedJson <- render(json).right
        body <- \/.fromTryCatchNonFatal[String](compact(renderedJson))
      } yield RequestBody(body)) leftMap (e => JsonSerialisationError)
    }
  }

  def toJson(request: DeleteItemRequest)(implicit formats: Formats): JValue = {
    ("Key" -> request.key.map(k => (k._1, decompose(k._2)))) ~
      ("ReturnValues" -> request.returnValues) ~
      ("TableName" -> request.table.value)
  }
}
