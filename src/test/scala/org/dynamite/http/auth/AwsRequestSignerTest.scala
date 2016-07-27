package org.dynamite.http.auth

import java.time.LocalDateTime

import org.dynamite.dsl._
import org.dynamite.http.{AmazonDateHeader, ContentTypeHeader, HostHeader}
import org.specs2.Specification

class AwsRequestSignerTest extends Specification { override def is = s2"""
      Specifications for the AwsRequestSigner
        Signing a request with Amazon predefined values should yield the expected result $signRequest
    """

  def signRequest = {
    Dummy.signRequest(
      HttpMethod.GET,
      Uri("/"),
      List("Action" -> List("ListUsers"), "Version" -> List("2010-05-08")),
      headers = List(
        ContentTypeHeader("application/x-www-form-urlencoded; charset=utf-8"),
        HostHeader(AwsHost("iam.amazonaws.com")),
        AmazonDateHeader(DateTimeStamp("20150830T123600Z"))),
      RequestBody(""),
      AwsDate(LocalDateTime.of(2015, 8, 30, 12, 36, 0)),
      AwsRegion.US_EAST_1,
      AwsService("iam"),
      AwsCredentials(
        AwsAccessKey("AKIDEXAMPLE"),
        AwsSecretKey("wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY"))) fold(
      err => ko("The signing method is expected to work for this case"),
      succ => succ must be_==(
        AwsSigningHeaders(
          AwsSigningCredentials("AKIDEXAMPLE/20150830/us-east-1/iam/aws4_request"),
          AwsSignedHeaders("content-type;host;x-amz-date"),
          AwsSignature("5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7"))))
  }

  private[this] object Dummy extends AwsRequestSigner

}
