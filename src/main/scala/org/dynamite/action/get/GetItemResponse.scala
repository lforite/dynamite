package org.dynamite.action.get

import io.circe.Json
import org.dynamite.dsl.JsonDeserializable

private[dynamite] case class GetItemResponse(item: Option[Json])

private[dynamite] object GetItemResponse {
  implicit val fromJson = new JsonDeserializable[GetItemResponse] {
    override def deserialize(json: Json): GetItemResponse =
      GetItemResponse(json.asObject.flatMap(_.toMap.get("Item")))
  }
}