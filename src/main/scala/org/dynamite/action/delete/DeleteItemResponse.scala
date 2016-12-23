package org.dynamite.action.delete

import org.dynamite.dsl.JsonDeserializable
import org.json4s._

private[dynamite] case class DeleteItemResponse(attributes: JValue)

private[dynamite] object DeleteItemResponse {
  implicit val fromJson = new JsonDeserializable[DeleteItemResponse] {
    override def deserialize(jValue: JValue): DeleteItemResponse =
      DeleteItemResponse(jValue \ "Attributes")
  }
}