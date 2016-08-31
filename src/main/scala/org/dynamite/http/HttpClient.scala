package org.dynamite.http

import java.net.{ConnectException, URISyntaxException}

import com.ning.http.client.Response
import dispatch._
import org.dynamite.dsl._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.{EitherT, \/}

private[dynamite] object HttpClient {

  def httpRequest(req: AwsHttpRequest)
    (implicit ex: ExecutionContext): EitherT[Future, DynamoError, AwsHttpResponse] = {
    EitherT.fromDisjunction[Future] {
      validAwsHost(req.host)
    } flatMap { awsHost =>
      EitherT.fromEither[Future, Throwable, Response] {
        Http {
          host(req.host.value).secure <<
            req.requestBody.value <:<
            req.signedHeaders.map(_.render)
        } either
      } leftMap[DynamoError] {
        case ce: ConnectException => UnreachableHostError(req.host.value)
        case _: Throwable => BasicDynamoError()
      }
    } flatMapF { resp =>
      Future.successful(toResponseBody(resp))
    }
  }

  private[this] def validAwsHost(awsHost: AwsHost): DynamoError \/ AwsHost = {
    try {
      new java.net.URI(awsHost.value)
      awsHost.right
    } catch {
      case u: URISyntaxException => InvalidHostError(awsHost.value).left
    }
  }

  private[this] def toResponseBody(resp: Response): DynamoError \/ AwsHttpResponse = {
    for {
      responseBody <- RequestExtractor.extract(resp)
      _ <- println(responseBody).right
    } yield AwsHttpResponse(
      org.dynamite.dsl.StatusCode(resp.getStatusCode),
      ResponseBody(responseBody))
  }
}