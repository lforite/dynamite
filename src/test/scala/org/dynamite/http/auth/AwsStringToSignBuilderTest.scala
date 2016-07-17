package org.dynamite.http.auth

import java.time.LocalDateTime

import org.dynamite.dsl._
import org.specs2.Specification

class AwsStringToSignBuilderTest extends Specification { override def is = s2"""
      Specifications for the AwsStringToSignBuilder
        The String To Sign should be correctly built $stringToSignOk
    """

  /** Example from http://docs.aws.amazon.com/general/latest/gr/sigv4-create-string-to-sign.html */
  def stringToSignOk = {
    Dummy.stringToSign(
      AwsDate(LocalDateTime.of(2015, 8, 30, 12, 36, 0)),
      AwsRegion("us-east-1"),
      AwsService("iam"),
      AwsCanonicalRequest("GET\n" +
        "/\n" +
        "Action=ListUsers&Version=2010-05-08\n" +
        "content-type:application/x-www-form-urlencoded; charset=utf-8\n" +
        "host:iam.amazonaws.com\nx-amz-date:20150830T123600Z\n\n" +
        "content-type;host;x-amz-date\n" +
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
    ) fold (
      err => ko("The method should be succeeding"),
      succ => succ must be_==(AwsStringToSign("AWS4-HMAC-SHA256\n20150830T123600Z\n20150830/us-east-1/iam/aws4_request\nf536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59"))
      )
  }

  private[this] object Dummy extends AwsStringToSignBuilder
}
