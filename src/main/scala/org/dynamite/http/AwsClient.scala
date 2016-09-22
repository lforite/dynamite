package org.dynamite.http

import java.time.{ZoneOffset, ZonedDateTime}

import org.dynamite.dsl._
import org.dynamite.http.auth.AwsRequestSigner

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.{EitherT, \/}

private[dynamite] object AwsClient {
  def post[REQUEST: JsonSerializable, RESPONSE: JsonDeserializable, RESULT, ERR >: DynamoCommonError](
    request: REQUEST,
    region: AwsRegion,
    credentials: AwsCredentials,
    targetHeader: AmazonTargetHeader)
    (respToRes: RESPONSE => RESULT)
    (implicit
      ec: ExecutionContext,
      protocol: DynamoProtocol[REQUEST, RESPONSE, RESULT, ERR]):
  Future[Either[ERR, RESULT]] = {
    EitherT.fromDisjunction[Future] {
      buildRequest(request, region, credentials, targetHeader)
    } flatMap {
      HttpClient.httpRequest
    } leftMap {
      _.asInstanceOf[ERR]
    } flatMap { res =>
      EitherT.fromDisjunction[Future] {
        toResult(res, respToRes, protocol.toErrors)
      }
    } toEither
  }

  private def buildRequest[REQUEST: JsonSerializable](
    request: REQUEST,
    region: AwsRegion,
    credentials: AwsCredentials,
    targetHeader: AmazonTargetHeader): DynamoCommonError \/ AwsHttpRequest = {
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
  }

  private[this] def toResult[RESPONSE: JsonDeserializable, RESULT, ERR >: DynamoCommonError](
    res: AwsHttpResponse,
    respToRes: RESPONSE => RESULT,
    toErrors: PartialFunction[AwsError, ERR]): ERR \/ RESULT = {
    for {
      _ <- checkErrors(res, toErrors)
      json <- RequestParser.parse(res.responseBody.value)
      response <- JsonDeserializable[RESPONSE].deserialize(json).right
      result <- respToRes(response).right
    } yield result
  }


  /**
    * According to http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Programming.Errors.html#Programming.Errors.Components
    */
  //here we need a partial function that is going to take an awsResponse and transform it to a ERR
  private[this] def checkErrors[ERR >: DynamoCommonError](
    awsHttpResponse: AwsHttpResponse,
    toErrors: PartialFunction[AwsError, ERR]): ERR \/ AwsHttpResponse = {
    awsHttpResponse.statusCode.value match {
      case 200 => awsHttpResponse.right
      case _ => RequestParser.parseTo[AwsError](awsHttpResponse.responseBody.value).map(toErrors).flatMap(_.left)
    }
  }
}