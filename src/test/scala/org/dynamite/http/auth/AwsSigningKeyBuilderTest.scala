package org.dynamite.http.auth

import org.dynamite.dsl._
import org.specs2.mutable.Specification

class AwsSigningKeyBuilderTest extends Specification { override def is = s2""""
      Specifications for the AwsRequestSigner
        AwsRequestSigner should sign a request correctly $sign
      """"

  /** According to http://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html */
  def sign = {
    val secretKey = AwsSecretKey("wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY")
    val dateStamp = DateStamp("20120215")
    val regionName = AwsRegion("us-east-1")
    val serviceName = AwsService("iam")
    val credentials = AwsCredentials(AwsAccessKey("anyAccessKey"), secretKey)

    Dummy.derive(credentials, dateStamp, regionName, serviceName) fold(
      s => ko,
      succ => Dummy.toHexFormat(succ.value) must be_==("f4780e2d9f65fa895f9c67b32ce1baf0b0d8a43505a000a1a9e090d414db404d")
      )
  }

  private[this] object Dummy extends AwsSigningKeyBuilder
}
