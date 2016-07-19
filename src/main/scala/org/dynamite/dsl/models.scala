package org.dynamite.dsl

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

case class AwsCredentials(accessKey: AwsAccessKey, secretKey: AwsSecretKey)

case class AwsAccessKey(value: String)

case class AwsSecretKey(value: String)

case class AwsDate(value: LocalDateTime) {
  lazy val date = DateStamp(value.format(AwsDate.dateFormatter))
  lazy val dateTime = DateTimeStamp(value.format(AwsDate.timeFormatter))
}

case class DateStamp(value: String)
case class DateTimeStamp(value: String)

object AwsDate {
  private lazy val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("UTC"))
  private lazy val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneId.of("UTC"))
}

case class AwsRegion(value: String)

case class AwsService(value: String)

case class ClientConfiguration(host: String, table: String)

case class AwsAuthorization(credential: String, signature: String)

case class AwsSigningKey(value: Array[Byte])

case class AwsStringToSign(value: String)

case class AwsCanonicalRequest(value: String)

case class AwsSignature(value: String)
