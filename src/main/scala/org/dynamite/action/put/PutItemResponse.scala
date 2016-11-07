package org.dynamite.action.put

import org.dynamite.dsl.JsonDeserializable
import org.json4s._


private[dynamite] case class PutItemResponse(attributes: JValue)

private[dynamite] object PutItemResponse {
  implicit val fromJson = new JsonDeserializable[PutItemResponse] {
    override def deserialize(jValue: JValue): PutItemResponse =
      PutItemResponse(jValue \ "Attributes")
  }
}
