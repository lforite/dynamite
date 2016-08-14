package org.dynamite.http

import java.net.ConnectException

import com.ning.http.client.Response
import dispatch._
import org.dynamite.dsl._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.{EitherT, \/}

trait HttpClient extends RequestExtractor {

  protected[dynamite] def httpRequest(req: AwsHttpRequest)
    (implicit ex: ExecutionContext): EitherT[Future, DynamoError, AwsHttpResponse] = {
    EitherT.fromEither[Future, Throwable, Response] {
      Http {
        host(req.host.value).secure <<
          req.requestBody.value <:<
          req.signedHeaders.map(_.render)
      } either
    } leftMap[DynamoError] {
      case ce: ConnectException => UnreachableHostException(req.host.value)
      case _: Throwable => BasicDynamoError()
    } flatMapF { resp =>
      Future.successful(toResponseBody(resp))
    }
  }

  private[this] def toResponseBody(resp: Response): DynamoError \/ AwsHttpResponse = {
    for {
      responseBody <- extract(resp)
      _ <- println(responseBody).right
    } yield AwsHttpResponse(
      org.dynamite.dsl.StatusCode(resp.getStatusCode),
      ResponseBody(responseBody))
  }
}