package org.dynamite.action.delete

import io.circe.Json
import org.dynamite.dsl.JsonDeserializable

private[dynamite] case class DeleteItemResponse(attributes: Option[Json]) extends AnyVal

private[dynamite] object DeleteItemResponse {
  implicit val fromJson = new JsonDeserializable[DeleteItemResponse] {
    override def deserialize(json: Json): DeleteItemResponse =
      DeleteItemResponse(json.asObject.flatMap(_.toMap.get("Attributes")))
  }
}