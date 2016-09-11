package org.dynamite.http

import java.time.{ZoneOffset, ZonedDateTime}

import org.dynamite.dsl._
import org.dynamite.http.auth.AwsRequestSigner

import scala.concurrent.{ExecutionContext, Future}
import scalaz.EitherT
import scalaz.Scalaz._

private[dynamite] object AwsClient {
  def post[REQUEST: JsonSerializable, RESPONSE: JsonDeserializable, RESULT](
    request: REQUEST,
    region: AwsRegion,
    credentials: AwsCredentials,
    targetHeader: AmazonTargetHeader)
    (respToRes: RESPONSE => RESULT)
    (implicit
      ec: ExecutionContext,
      protocol: DynamoProtocol[REQUEST, RESPONSE, RESULT]):
  Future[Either[DynamoError, RESULT]] = {
    EitherT.fromDisjunction[Future] {
      for {
        dateStamp <- AwsDate(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime).right
        headers <- (
          AcceptEncodingHeader("identity") ::
            ContentTypeHeader("application/x-amz-json-1.0") ::
            AmazonDateHeader(dateStamp.dateTime) ::
            HostHeader(region.endpoint) ::
            targetHeader ::
            Nil).right
        requestBody <- JsonSerializable[REQUEST].serialize(request)
        signingHeaders <- AwsRequestSigner.signRequest(
          httpMethod = HttpMethod.POST,
          uri = Uri("/"),
          queryParameters = List(),
          headers = headers,
          requestBody = requestBody,
          awsDate = dateStamp,
          awsRegion = region,
          awsService = AwsService("dynamodb"),
          awsCredentials = credentials)
        signedHeaders <- (AuthorizationHeader(signingHeaders) :: headers).right
      } yield AwsHttpRequest(region.endpoint, requestBody, signedHeaders)
    } flatMap {
      HttpClient.httpRequest
    } flatMapF { res =>
      Future {
        for {
          json <- RequestParser.parse(res.responseBody.value)
          response <- JsonDeserializable[RESPONSE].deserialize(json).right
          result <- respToRes(response).right
        } yield result
      }
    } toEither
  }
}
