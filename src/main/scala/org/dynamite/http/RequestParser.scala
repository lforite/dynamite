package org.dynamite.http

import com.ning.http.client.Response
import org.dynamite.dsl.{DynamoCommonError, JsonDeserialisationError}
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods.{parse => jparse}

import scalaz.\/

private[dynamite] object RequestParser {

  def parse(jsonString: String): DynamoCommonError \/ JValue =
    \/.fromTryCatchNonFatal[JValue](jparse(jsonString)) leftMap {
      _: Throwable =>
        //todo: add proper debug log here
        JsonDeserialisationError(s"The json $jsonString is not a valid string and was impossible to parse.")
    }


  def parseTo[A](jsonString: String)(implicit mf: scala.reflect.Manifest[A]): DynamoCommonError \/ A = {
    import org.dynamite.dsl.Format._

    \/.fromTryCatchNonFatal[A](jparse(jsonString).extract[A]) leftMap {
      t: Throwable =>
        //todo: add proper debug log here
        println(t)
        JsonDeserialisationError(s"An error occurred while trying to deserialise $jsonString")
    }
  }
}


private[dynamite] object RequestExtractor {
  def extract(resp: Response): DynamoCommonError \/ String =
    \/.fromTryCatchNonFatal[String](resp.getResponseBody) leftMap {
      _: Throwable =>
        //todo: add proper debug log here
        JsonDeserialisationError("An error occurred while trying to read response from DynamoDB")
    }
}