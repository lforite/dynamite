package org.dynamite.http

import com.ning.http.client.Response
import org.dynamite.dsl.{BasicDynamoError, DynamoError}
import org.json4s._
import org.json4s.jackson.JsonMethods.{parse => jparse}

import scalaz.\/

private[dynamite] object RequestParser {
  def parse(jsonString: String): DynamoError \/ JValue =
    \/.fromTryCatchThrowable[JValue, Throwable](jparse(jsonString)) leftMap {
      _: Throwable => BasicDynamoError()
    }
}

private[dynamite] object RequestExtractor {
  def extract(resp: Response): DynamoError \/ String =
    \/.fromTryCatchThrowable[String, Throwable](resp.getResponseBody) leftMap {
      _: Throwable => BasicDynamoError()
    }
}