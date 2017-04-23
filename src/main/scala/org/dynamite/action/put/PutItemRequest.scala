package org.dynamite.action.put

import dynamo.ast.DynamoType
import io.circe.syntax._
import io.circe.{Encoder, Printer}
import org.dynamite.ast.AwsTypeSerialiser._
import org.dynamite.ast.ROOT
import org.dynamite.dsl._

import scalaz.{\/, \/-}

private[dynamite] case class PutItemRequest(item: DynamoType, table: AwsTable)

private[dynamite] object PutItemRequest {
  val printer: Printer = Printer.noSpaces.copy(dropNullKeys = true)

  implicit def toRequestBody[A] = new JsonSerializable[PutItemRequest] {
    def serialize(putItemRequest: PutItemRequest): DynamoCommonError \/ RequestBody = {
      \/-(RequestBody(printer.pretty(putItemRequest.asJson)))
    }
  }

  implicit val encoder: Encoder[PutItemRequest] = Encoder.forProduct2("Item", "TableName")(req => (ROOT(req.item), req.table.value))
}
