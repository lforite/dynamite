package org.dynamite.action.delete

import dynamo.ast.DynamoScalarType
import io.circe.syntax._
import io.circe.{Encoder, Printer}
import org.dynamite.ast.AwsTypeSerialiser._
import org.dynamite.dsl._

import scalaz.\/

private[dynamite] case class DeleteItemRequest(
  key: List[(String, DynamoScalarType)],
  returnValues: Option[String] = Some("NONE"),
  table: AwsTable
)

private[dynamite] object DeleteItemRequest {
  val printer: Printer = Printer.noSpaces.copy(dropNullKeys = true)
  implicit val DeleteItemRequestEncoder: Encoder[DeleteItemRequest] = Encoder.forProduct3("Key", "ReturnValues", "TableName")((request: DeleteItemRequest) => (request.key.toMap, request.returnValues, request.table.value))

  implicit val toRequestBody = new JsonSerializable[DeleteItemRequest] {
    def serialize(request: DeleteItemRequest): DynamoCommonError \/ RequestBody = {
      (for {
        body <- \/.fromTryCatchNonFatal[String](printer.pretty(request.asJson))
      } yield RequestBody(body)) leftMap (e => JsonSerialisationError)
    }
  }
}
