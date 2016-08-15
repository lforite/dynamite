package org.dynamite.dsl

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

import org.dynamite.http.HttpHeader

private[dynamite] case class AwsDate(value: LocalDateTime) {
  lazy val date = DateStamp(value.format(AwsDate.dateFormatter))
  lazy val dateTime = DateTimeStamp(value.format(AwsDate.timeFormatter))
}

private[dynamite] case class DateStamp(value: String)
private[dynamite] case class DateTimeStamp(value: String)

private[dynamite] object AwsDate {
  private lazy val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("UTC"))
  private lazy val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneId.of("UTC"))
}

private[dynamite] case class AwsService(value: String)
private[dynamite] case class AwsAuthorization(credential: String, signature: String)
private[dynamite] case class AwsSigningKey(value: Array[Byte])
private[dynamite] case class AwsStringToSign(value: String, scope: AwsScope)
private[dynamite] case class AwsScope(value: String)
private[dynamite] case class AwsCanonicalRequest(value: String, signedHeaders: AwsSignedHeaders)
private[dynamite] case class AwsSignature(value: String)

private[dynamite] sealed trait HttpMethod { val value: String}
private[dynamite] object HttpMethod {
  case object POST extends HttpMethod { val value = "POST" }
  case object GET extends HttpMethod { val value = "GET" }
}

private[dynamite] case class Uri(value: String)

private[dynamite] case class AwsHttpRequest(host: AwsHost, requestBody: RequestBody, signedHeaders: List[HttpHeader])
private[dynamite] case class RequestBody(value: String)

private[dynamite] case class AwsHttpResponse(statusCode: StatusCode, responseBody: ResponseBody)
private[dynamite] case class StatusCode(value: Int)
private[dynamite] case class ResponseBody(value: String)

private[dynamite] case class AwsSigningHeaders(signingCredentials: AwsSigningCredentials, signedHeaders: AwsSignedHeaders, awsSignature: AwsSignature)

private[dynamite] case class AwsSigningCredentials(value: String)

private[dynamite] case class AwsSignedHeaders(value: String)
