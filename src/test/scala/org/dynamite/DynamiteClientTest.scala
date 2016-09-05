package org.dynamite

import org.dynamite.dsl._
import org.dynamite.http.HttpServer
import org.specs2.Specification
import scala.concurrent.ExecutionContext.Implicits.global

class DynamiteClientTest extends Specification with HttpServer { def is = s2"""
      Specifications for the DynamiteClient
        Getting an existing item should return this item $get
  """

  lazy val configuration = ClientConfiguration(AwsTable("dummy_table"), AwsRegion.EU_CENTRAL_1)
  lazy val credentials = AwsCredentials(AwsAccessKey("test"), AwsSecretKey("test"))
  lazy val client = DynamiteClient(configuration, credentials)

  def get = withHttpServer { server =>
    ok
  }


  case class Dummy(test: String)

}
