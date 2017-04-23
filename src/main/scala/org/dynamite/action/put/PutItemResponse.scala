package org.dynamite.action.put

import io.circe.Json
import org.dynamite.dsl.JsonDeserializable


private[dynamite] case class PutItemResponse(attributes: Option[Json]) extends AnyVal

private[dynamite] object PutItemResponse {
  implicit val fromJson = new JsonDeserializable[PutItemResponse] {
    override def deserialize(json: Json): PutItemResponse =
      PutItemResponse(json.asObject.flatMap(_.toMap.get("Attributes")))
  }
}
