package org.dynamite.http.auth

import org.dynamite.dsl._
import org.dynamite.http.{AmazonDateHeader, ContentTypeHeader, HostHeader}
import org.specs2.Specification

class AwsCanonicalRequestBuilderTest extends Specification { override def is = s2"""
      Specifications for the Canonical request creator
        Creating a canonical request with AWS example should yield the expected value $createCanonicalRequest
   """

  /** Example from http://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html */
  def createCanonicalRequest = {
    Dummy.canonicalRequest(
      httpMethod = HttpMethod.GET,
      uri = Uri("/"),
      queryParameters = List("Action" -> List("ListUsers"), "Version" -> List("2010-05-08")),
      headers = List(
        ContentTypeHeader("application/x-www-form-urlencoded; charset=utf-8"),
        HostHeader(AwsHost("iam.amazonaws.com")),
        AmazonDateHeader(DateTimeStamp("20150830T123600Z"))),
      RequestBody("")
    ) fold(
      err => ko("The canonical request creation should succeed"),
      succ => succ must be_==(AwsCanonicalRequest("GET\n" +
        "/\n" +
        "Action=ListUsers&Version=2010-05-08\n" +
        "content-type:application/x-www-form-urlencoded; charset=utf-8\n" +
        "host:iam.amazonaws.com\n" +
        "x-amz-date:20150830T123600Z\n\n" +
        "content-type;host;x-amz-date\n" +
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
        AwsSignedHeaders("content-type;host;x-amz-date")))
      )
  }

  private[this] object Dummy extends AwsCanonicalRequestBuilder

}
