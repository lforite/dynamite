package org.dynamite.dsl

case class ClientConfiguration(table: AwsTable, awsRegion: AwsRegion, host: Option[AwsHost] = None)
case class AwsCredentials(accessKey: AwsAccessKey, secretKey: AwsSecretKey)
case class AwsAccessKey(value: String)
case class AwsSecretKey(value: String)
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
