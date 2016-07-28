package org.dynamite.http

import java.net.ConnectException

import com.ning.http.client.Response
import dispatch.Defaults._
import dispatch._
import org.dynamite.dsl.{BasicDynamoError, DynamoError, UnreachableHostException}

import scala.concurrent.Future
import scalaz.EitherT
import scalaz.Scalaz._

trait HttpClient {
  protected[dynamite] def httpRequest(req: Req): EitherT[Future, DynamoError, Response] = {
    EitherT.fromEither(Http(req).either) leftMap {
      case ce: ConnectException => UnreachableHostException(req.url)
      case _: Throwable => BasicDynamoError()
    }
  }
}