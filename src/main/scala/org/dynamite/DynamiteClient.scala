package org.dynamite

import java.time.{ZoneOffset, ZonedDateTime}

import com.ning.http.client.Response
import dispatch.Defaults._
import dispatch._
import org.dynamite.ast.{AwsJsonReader, AwsJsonWriter, AwsScalarType, AwsTypeSerializer}
import org.dynamite.dsl.GetItemRequest.toJson
import org.dynamite.dsl._
import org.dynamite.http._
import org.dynamite.http.auth.AwsRequestSigner
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._

import scala.concurrent.Future
import scalaz.Scalaz._
import scalaz.{EitherT, \/}

trait DynamoClient[A] {

  type DynamoAction[B] = Future[Either[DynamoError, B]]

  def get(primaryKey: (String, AwsScalarType), sortKey: Option[(String, AwsScalarType)]): DynamoAction[Option[A]]

  def put(a: A): DynamoAction[Boolean]

  def delete(id: String): DynamoAction[Boolean]
}

case class DynamiteClient[A](
  configuration: ClientConfiguration,
  credentials: AwsCredentials)(implicit m: Manifest[A])
  extends DynamoClient[A]
    with AwsJsonWriter
    with AwsJsonReader
    with AwsRequestSigner
    with RequestParser
    with RequestExtractor {

  implicit private val formats = DefaultFormats + new AwsTypeSerializer

  private val getHeaders = List(
    AcceptEncodingHeader("identity"),
    ContentTypeHeader("application/x-amz-json-1.0"),
    AmazonTargetHeader("DynamoDB_20120810.GetItem"))

  override def get(
    primaryKey: (String, AwsScalarType),
    sortKey: Option[(String, AwsScalarType)] = None): DynamoAction[Option[A]] = {

    val request: DynamoError \/ Req = for {
      dateStamp <- AwsDate(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime).right
      getItemRequest <- GetItemRequest(
        key = (Some(primaryKey) :: sortKey :: Nil).flatten,
        table = configuration.table).right
      json <- \/.fromTryCatchThrowable[String, Throwable](compact(render(toJson(getItemRequest)))) leftMap (e => BasicDynamoError())
      headers <- (
        AmazonDateHeader(dateStamp.dateTime) ::
          HostHeader(configuration.host) ::
          getHeaders).right
      signingHeaders <- signRequest(
        httpMethod = HttpMethod.POST,
        uri = Uri("/"),
        queryParameters = List(),
        headers = headers,
        requestBody = RequestBody(json),
        awsDate = dateStamp,
        awsRegion = configuration.awsRegion,
        awsService = AwsService("dynamodb"),
        awsCredentials = credentials)
      signedHeaders <- (AuthorizationHeader(signingHeaders) :: headers).map(_.render).right
      req <- (host(configuration.host).secure << json <:< signedHeaders).right
    } yield req

    EitherT.fromDisjunction[Future](request) flatMap { req =>
      EitherT.fromEither(Http(req).either).leftMap(e => BasicDynamoError())
    } flatMapF { r =>
      Future(handleResponse(r))
    } toEither
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
