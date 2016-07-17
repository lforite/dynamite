package org.dynamite

import java.time.LocalDateTime
import java.util.Date

import com.ning.http.client.Response
import dispatch.Defaults._
import dispatch._
import org.dynamite.ast.{AwsJsonReader, AwsJsonWriter}
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

  def get(id: String): DynamoAction[Option[A]]

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
    with RequestExtractor
    with RequestParser {

  implicit private val formats = DefaultFormats

  private val getHeaders = List(
    AcceptEncodingHeader("identity"),
    ContentTypeHeader("application/x-amz-json-1.0"),
    AmazonTargetHeader("DynamoDB_20120810.GetItem"))

  override def get(id: String): DynamoAction[Option[A]] = {
    val request: DynamoError \/ Req = for {
      dateStamp <- AwsDate(LocalDateTime.now()).right
      auth <- sign(credentials, dateStamp.date, AwsRegion("eu-west-1"), AwsService("dynamodb"))
      headers <- (
        AuthorizationHeader(auth) ::
          AmazonDateHeader(dateStamp.dateTime) ::
          HostHeader(configuration.host) ::
          getHeaders
        ).map(_.render).toMap.right
      request <- GetItemRequest(key = Map("id" -> Map("S" -> id)), table = configuration.table).right
      json <- compact(render(toJson(request))).right
      req <- (url(configuration.host) << json <:< headers).right
    } yield req

    EitherT.fromDisjunction[Future](request) flatMap { req =>
      EitherT.fromEither(Http(req).either).leftMap(e => {
        println(e)
        BasicDynamoError()
      }
      )
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
