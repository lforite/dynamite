package org.dynamite

import java.time.{ZoneOffset, ZonedDateTime}

import com.ning.http.client.Response
import dispatch._
import org.dynamite.ast.{AwsJsonReader, AwsJsonWriter, AwsScalarType, AwsTypeSerializer}
import org.dynamite.dsl._
import org.dynamite.http._
import org.dynamite.http.auth.AwsRequestSigner
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.{EitherT, \/}

trait DynamoClient[A] {

  type DynamoAction[B] = Future[Either[DynamoError, B]]

  def get(
    primaryKey: (String, AwsScalarType),
    sortKey: Option[(String, AwsScalarType)],
    consistentRead: Boolean): DynamoAction[Option[A]]

  def put(a: A): DynamoAction[Boolean]

  def delete(id: String): DynamoAction[Boolean]
}

case class DynamiteClient[A](
  configuration: ClientConfiguration,
  credentials: AwsCredentials)(implicit m: Manifest[A], ec: ExecutionContext)
  extends DynamoClient[A]
    with AwsJsonWriter
    with AwsJsonReader
    with AwsRequestSigner
    with RequestParser
    with RequestExtractor
    with HttpClient {

  implicit private val formats = DefaultFormats + new AwsTypeSerializer

  private val getHeaders = List(
    AcceptEncodingHeader("identity"),
    ContentTypeHeader("application/x-amz-json-1.0"),
    AmazonTargetHeader("DynamoDB_20120810.GetItem"))

  override def get(
    primaryKey: (String, AwsScalarType),
    sortKey: Option[(String, AwsScalarType)] = None,
    consistentRead: Boolean = false): DynamoAction[Option[A]] = {

    val request: DynamoError \/ Req = for {
      awsHost <- configuration.host.getOrElse(configuration.awsRegion.endpoint).right
      dateStamp <- AwsDate(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime).right
      getItemRequest <- GetItemRequest(
        key = (Some(primaryKey) :: sortKey :: Nil).flatten,
        table = configuration.table,
        consistentRead = consistentRead).right
      requestBody <- toRequestBody(getItemRequest)
      headers <- (
        AmazonDateHeader(dateStamp.dateTime) ::
          HostHeader(awsHost) ::
          getHeaders).right
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
      signedHeaders <- (AuthorizationHeader(signingHeaders) :: headers).map(_.render).right
      req <- (host(awsHost.value).secure << requestBody.value <:< signedHeaders).right
    } yield req

    EitherT.fromDisjunction[Future](request) flatMap {
      httpRequest
    } flatMapF { r =>
      Future.successful(handleResponse(r))
    } toEither
  }

  private def toRequestBody(getItemRequest: GetItemRequest): DynamoError \/ RequestBody = {
    (for {
      json <- GetItemRequest.toJson(getItemRequest).right
      renderedJson <- render(json).right
      body <- \/.fromTryCatchThrowable[String, Throwable](compact(renderedJson))
    } yield RequestBody(body)) leftMap (e => JsonSerialisationError)
  }

  private def handleResponse(res: Response): DynamoError \/ Option[A] =
    for {
      responseBody <- extract(res)
      _ <- println(responseBody).right
      json <- parse(responseBody)
      getResponse <- GetItemResponse.fromJson(json).right
      transformed <- fromAws(getResponse.item).right
    } yield transformed.extractOpt[A]


  override def put(a: A): DynamoAction[Boolean] = ???

  override def delete(id: String): DynamoAction[Boolean] = ???

}
