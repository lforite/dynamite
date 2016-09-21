package org.dynamite.http

import com.ning.http.client.Response
import org.dynamite.dsl.{BasicDynamoError, DynamoCommonError}
import org.json4s._
import org.json4s.jackson.JsonMethods.{parse => jparse}

import scalaz.\/

private[dynamite] object RequestParser {
  implicit private val formats = DefaultFormats

  def parse(jsonString: String): DynamoCommonError \/ JValue =
    \/.fromTryCatchThrowable[JValue, Throwable](jparse(jsonString)) leftMap {
      _: Throwable => BasicDynamoError()
    }

  def parseTo[A](jsonString: String)(implicit mf: scala.reflect.Manifest[A]): DynamoCommonError \/ A =
    \/.fromTryCatchThrowable[A, Throwable](jparse(jsonString).extract[A]) leftMap {
      _: Throwable => BasicDynamoError()
    }
}

private[dynamite] object RequestExtractor {
  def extract(resp: Response): DynamoCommonError \/ String =
    \/.fromTryCatchThrowable[String, Throwable](resp.getResponseBody) leftMap {
      _: Throwable => BasicDynamoError()
    }
}