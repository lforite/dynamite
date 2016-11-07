package org.dynamite.action.get

import org.dynamite.dsl.JsonDeserializable
import org.json4s._

private[dynamite] case class GetItemResponse(item: JValue)

private[dynamite] object GetItemResponse {
  implicit val fromJson = new JsonDeserializable[GetItemResponse] {
    override def deserialize(jValue: JValue): GetItemResponse =
      GetItemResponse(jValue \ "Item")
  }
}