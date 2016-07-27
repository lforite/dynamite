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

case class AwsTable(value: String)
case class AwsRegionName(value: String)
case class AwsHost(value: String)

sealed trait AwsRegion {
  val name: AwsRegionName
  val endpoint: AwsHost
}

object AwsRegion {

  /** US East (N. Virginia) */
  case object US_EAST_1 extends AwsRegion {
    val name = AwsRegionName("us-east-1")
    val endpoint = AwsHost("dynamodb.us-east-1.amazonaws.com")
  }

  /** US West (N. California) */
  case object US_WEST_1 extends AwsRegion {
    val name = AwsRegionName("us-west-1")
    val endpoint = AwsHost("dynamodb.us-west-1.amazonaws.com")
  }

  /** US West (Oregon) */
  case object US_WEST_2 extends AwsRegion {
    val name = AwsRegionName("us-west-2")
    val endpoint = AwsHost("dynamodb.us-west-2.amazonaws.com")
  }

  /** EU (Ireland) */
  case object EU_WEST_1 extends AwsRegion {
    val name = AwsRegionName("eu-west-1")
    val endpoint = AwsHost("dynamodb.eu-west-1.amazonaws.com")
  }

  /** EU (Frankfurt) */
  case object EU_CENTRAL_1 extends AwsRegion {
    val name = AwsRegionName("eu-central-1")
    val endpoint = AwsHost("dynamodb.eu-central-1.amazonaws.com")
  }

  /** Asia Pacific (Mumbai) */
  case object AP_SOUTH_1 extends AwsRegion {
    val name = AwsRegionName("ap-south-1")
    val endpoint = AwsHost("dynamodb.ap-south-1.amazonaws.com")
  }

  /** Asia Pacific (Singapore) */
  case object AP_SOUTHEAST_1 extends AwsRegion {
    val name = AwsRegionName("ap-southeast-1")
    val endpoint = AwsHost("dynamodb.ap-southeast-1.amazonaws.com")
  }

  /** Asia Pacific (Sydney)	 */
  case object AP_SOUTHEAST_2 extends AwsRegion {
    val name = AwsRegionName("ap-southeast-2")
    val endpoint = AwsHost("dynamodb.ap-southeast-2.amazonaws.com")
  }

  /** Asia Pacific (Tokyo) */
  case object AP_NORTHEAST_1 extends AwsRegion {
    val name = AwsRegionName("ap-northeast-1")
    val endpoint = AwsHost("dynamodb.ap-northeast-1.amazonaws.com")
  }

  /** Asia Pacific (Seoul) */
  case object AP_NORTHEAST_2 extends AwsRegion {
    val name = AwsRegionName("ap-northeast-2")
    val endpoint = AwsHost("dynamodb.ap-northeast-2.amazonaws.com")
  }

  /** South America (São Paulo) */
  case object SA_EAST_1 extends AwsRegion {
    val name = AwsRegionName("sa-east-1")
    val endpoint = AwsHost("dynamodb.sa-east-1.amazonaws.com")
  }
}

case class AwsService(value: String)

case class ClientConfiguration(table: AwsTable, awsRegion: AwsRegion, host: Option[AwsHost] = None)

case class AwsAuthorization(credential: String, signature: String)

case class AwsSigningKey(value: Array[Byte])

case class AwsStringToSign(value: String, scope: AwsScope)

case class AwsScope(value: String)

case class AwsCanonicalRequest(value: String, signedHeaders: AwsSignedHeaders)

case class AwsSignature(value: String)


sealed trait HttpMethod { val value: String}
object HttpMethod {
  case object POST extends HttpMethod { val value = "POST" }
  case object GET extends HttpMethod { val value = "GET" }
}

case class Uri(value: String)

case class RequestBody(value: String)

case class AwsSigningHeaders(signingCredentials: AwsSigningCredentials, signedHeaders: AwsSignedHeaders, awsSignature: AwsSignature)

case class AwsSigningCredentials(value: String)

case class AwsSignedHeaders(value: String)
