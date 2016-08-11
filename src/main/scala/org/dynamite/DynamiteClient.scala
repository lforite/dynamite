package org.dynamite

import java.time.{ZoneOffset, ZonedDateTime}

import org.dynamite.ast.{AwsJsonReader, AwsJsonWriter, AwsScalarType, AwsTypeSerializer}
import org.dynamite.dsl.{GetItemRequest, _}
import org.dynamite.http._
import org.dynamite.http.auth.AwsRequestSigner
import org.json4s.DefaultFormats

import scala.concurrent.{ExecutionContext, Future}
import scalaz.EitherT
import scalaz.Scalaz._

trait DynamoClient[A] {

  def get(
    primaryKey: (String, AwsScalarType),
    sortKey: Option[(String, AwsScalarType)],
    consistentRead: Boolean)(implicit ec: ExecutionContext): Future[Either[DynamoError, GetItemResult[A]]]
}

case class DynamiteClient[A](
  configuration: ClientConfiguration,
  credentials: AwsCredentials)(implicit m: Manifest[A], ec: ExecutionContext)
  extends DynamoClient[A]
    with AwsJsonWriter
    with AwsJsonReader
    with AwsRequestSigner
    with RequestParser
    with HttpClient {

  implicit private val formats = DefaultFormats + new AwsTypeSerializer

  override def get(
    primaryKey: (String, AwsScalarType),
    sortKey: Option[(String, AwsScalarType)] = None,
    consistentRead: Boolean = false)
    (implicit ec: ExecutionContext): Future[Either[DynamoError, GetItemResult[A]]] = {
    requestAws[GetItemRequest, GetItemResponse, GetItemResult[A]](
      GetItemRequest(
        key = (Some(primaryKey) :: sortKey :: Nil).flatten,
        table = configuration.table,
        consistentRead = consistentRead),
      (res: GetItemResponse) =>
        GetItemResult[A](fromAws(res.item).extractOpt[A]))
  }

  private def requestAws[REQUEST, RESPONSE, RESULT](
    request: REQUEST,
    respToRes: RESPONSE => RESULT)
    (implicit
      requestable: JsonSerializable[REQUEST] with HasHeader[REQUEST],
      fromJson: FromJson[RESPONSE],
      ec: ExecutionContext): Future[Either[DynamoError, RESULT]] = {
    EitherT.fromDisjunction[Future] {
      for {
        awsHost <- configuration.host.getOrElse(configuration.awsRegion.endpoint).right
        dateStamp <- AwsDate(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime).right
        headers <- (
          AmazonDateHeader(dateStamp.dateTime) ::
            HostHeader(awsHost) ::
            requestable.headers).right
        requestBody <- requestable.serialize(request)
        signingHeaders <- signRequest(
          httpMethod = HttpMethod.POST,
          uri = Uri("/"),
          queryParameters = List(),
          headers = headers,
          requestBody = requestBody,
          awsDate = dateStamp,
          awsRegion = configuration.awsRegion,
          awsService = AwsService("dynamodb"),
          awsCredentials = credentials)
        signedHeaders <- (AuthorizationHeader(signingHeaders) :: headers).right
      } yield AwsHttpRequest(awsHost, requestBody, signedHeaders)
    } flatMap {
      httpRequest
    } flatMapF { res =>
      Future {
        for {
          json <- parse(res.responseBody.value)
          response <- fromJson.fromJson(json).right
          result <- respToRes(response).right
        } yield result
      }
    } toEither
  }

}