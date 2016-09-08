package org.dynamite.http

import org.dynamite.dsl.{AwsHost, AwsSigningHeaders, DateTimeStamp}

private[dynamite] trait HttpHeader {
  def render: (String, String)
}

private[dynamite] case class AcceptEncodingHeader(value: String) extends HttpHeader {
  def render = "Accept-Encoding" -> value
}

private[dynamite] case class ContentTypeHeader(contentType: String) extends HttpHeader {
  def render = "Content-Type" -> contentType
}

//see http://docs.aws.amazon.com/general/latest/gr/sigv4-calculate-signature.html
private[dynamite] case class AuthorizationHeader(value: AwsSigningHeaders) extends HttpHeader {
  def render = "Authorization" -> s"AWS4-HMAC-SHA256 Credential=${value.signingCredentials.value}, SignedHeaders=${value.signedHeaders.value}, Signature=${value.awsSignature.value}"
}

private[dynamite] case class AmazonDateHeader(dateTimeStamp: DateTimeStamp) extends HttpHeader {
  def render = "X-Amz-Date" -> dateTimeStamp.value
}

private[dynamite] case class AmazonTargetHeader(value: String) extends HttpHeader {
  def render = "X-Amz-Target" -> value
}

private[dynamite] case class HostHeader(value: AwsHost) extends HttpHeader {
  def render = "Host" -> value.value
}