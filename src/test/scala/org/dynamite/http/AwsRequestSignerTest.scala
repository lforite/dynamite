package org.dynamite.http

import org.dynamite.dsl.AwsCredentials
import org.specs2.mutable.Specification

class AwsRequestSignerTest extends Specification { override def is = s2""""
      Specifications for the AwsRequestSigner
        AwsRequestSigner should sign a request correctly $sign
      """"

  def sign = {
    val secretKey = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY"
    val dateStamp = "20120215"
    val regionName = "us-east-1"
    val serviceName = "iam"
    val credentials = AwsCredentials("anyAccessKey", secretKey)

    AwsRequestSigner.sign(credentials, dateStamp, regionName, serviceName) fold(
      s => ko,
      auth => auth.signature must be_==("f4780e2d9f65fa895f9c67b32ce1baf0b0d8a43505a000a1a9e090d414db404d")
      )
  }
}
