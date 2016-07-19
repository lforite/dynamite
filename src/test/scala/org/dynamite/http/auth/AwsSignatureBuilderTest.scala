package org.dynamite.http.auth

import org.dynamite.dsl.{AwsScope, AwsSigningKey, AwsStringToSign}
import org.specs2.Specification

class AwsSignatureBuilderTest extends Specification {
  override def is =
    s2"""
      Specifications for the SignatureBuilder
        The request should be signed directly given the example by Amazon $correctlySigned
    """

  def correctlySigned = {
    Dummy.sign(
      AwsSigningKey(Array(196.toByte, 175.toByte, 177.toByte, 204.toByte, 87.toByte, 113.toByte, 216.toByte, 113.toByte, 118.toByte, 58.toByte, 57.toByte, 62.toByte, 68.toByte, 183.toByte, 3.toByte, 87.toByte, 27.toByte, 85.toByte, 204.toByte, 40.toByte, 66.toByte, 77.toByte, 26.toByte, 94.toByte, 134.toByte, 218.toByte, 110.toByte, 211.toByte, 193.toByte, 84.toByte, 164.toByte, 185.toByte)),
      AwsStringToSign(
        "AWS4-HMAC-SHA256\n" +
          "20150830T123600Z\n" +
          "20150830/us-east-1/iam/aws4_request\n" +
          "f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59",
          AwsScope("20150830/us-east-1/iam/aws4_request"))) fold(
      err => ko("The signing should not yield an error"),
      succ => succ.value must be_==("5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7")
      )
  }

  private[this] object Dummy extends AwsSignatureBuilder

}
