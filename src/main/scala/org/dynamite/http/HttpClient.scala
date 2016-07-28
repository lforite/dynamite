package org.dynamite.http

import com.ning.http.client.Response
import dispatch.Defaults._
import dispatch._
import org.dynamite.dsl.{BasicDynamoError, DynamoError}

import scala.concurrent.Future
import scalaz.EitherT
import scalaz.Scalaz._

trait HttpClient {
  protected[dynamite] def httpRequest(req: Req): EitherT[Future, DynamoError, Response] = {
    EitherT.fromEither(Http(req).either) leftMap {
      case _: Throwable => BasicDynamoError()
    }
  }
}