package org.dynamite.http

import com.ning.http.client.Response
import io.circe.parser.{parse => jparse}
import io.circe.{Decoder, Json, _}
import org.dynamite.dsl.{DynamoCommonError, JsonDeserialisationError}

import scalaz.{-\/, \/, \/-}

private[dynamite] object RequestParser {

  def parse(jsonString: String): DynamoCommonError \/ Json =
    \/.fromEither(jparse(jsonString)) leftMap {
      parsingFailure: ParsingFailure =>
        //todo: add proper debug log here
        JsonDeserialisationError(s"The json $jsonString is not a valid string and was impossible to parse.")
    }


  def parseTo[A: Decoder](jsonString: String): DynamoCommonError \/ A = {
    jparse(jsonString) match {
      case Right(json) => Decoder[A].decodeJson(json) match {
        case Right(a) => \/-(a)
        case Left(decondingFailure) => -\/(JsonDeserialisationError(s"An error occurred while trying to deserialise $jsonString"))
      }
      case Left(parsingFailure) => -\/(JsonDeserialisationError(s"An error occurred while trying to deserialise $jsonString"))
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