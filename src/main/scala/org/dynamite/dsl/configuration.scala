package org.dynamite.dsl

/**
  * The client configuration to specify which table to query against
  * and in which region the database is located in. A configuration can be created as follow:
  *
  * {{{
  * ClientConfiguration(AwsTable("students"), AwsRegion.EU_WEST_1)
  * }}}
  *
  * @param table     The table to query against
  * @param awsRegion The AWS region where the database is located in. See [[org.dynamite.dsl.AwsRegion]]
  *                  for a complete list of available regions.
  */
case class ClientConfiguration(table: AwsTable, awsRegion: AwsRegion)

/**
  * The standard AWS credentials based on the access key and the secret key.
  *
  * @param accessKey The AWS Access Key.
  * @param secretKey The AWS Secret Key.
  */
case class AwsCredentials(accessKey: AwsAccessKey, secretKey: AwsSecretKey)

/**
  * The Aws Access Key
  *
  * @param value the value of the Aws Access Key
  */
case class AwsAccessKey(value: String) extends AnyVal

/**
  * The Aws Secret Key
  *
  * @param value the value of the Aws Secret Key
  */
case class AwsSecretKey(value: String) extends AnyVal

/**
  * The table to execute query against
  *
  * @param value the table name
  */
case class AwsTable(value: String) extends AnyVal

/**
  * The name of the region in which the DynamoDB instance is located in.
  *
  * @param value the name of the region
  */
case class AwsRegionName(value: String) extends AnyVal

/**
  * The host to reach the DynamoDB instance.
  *
  * @param value the host.
  */
case class AwsHost(value: String) extends AnyVal

trait AwsRegion {
  val name: AwsRegionName
  val endpoint: AwsHost
}

/**
  * The complete list of the available AWS regions for DynamoDB
  */
object AwsRegion {

  /** US East (N. Virginia) */
  case object US_EAST_1 extends AwsRegion {
    lazy val name = AwsRegionName("us-east-1")
    lazy val endpoint = AwsHost("dynamodb.us-east-1.amazonaws.com")
  }

  /** US West (N. California) */
  case object US_WEST_1 extends AwsRegion {
    lazy val name = AwsRegionName("us-west-1")
    lazy val endpoint = AwsHost("dynamodb.us-west-1.amazonaws.com")
  }

  /** US West (Oregon) */
  case object US_WEST_2 extends AwsRegion {
    lazy val name = AwsRegionName("us-west-2")
    lazy val endpoint = AwsHost("dynamodb.us-west-2.amazonaws.com")
  }

  /** EU (Ireland) */
  case object EU_WEST_1 extends AwsRegion {
    lazy val name = AwsRegionName("eu-west-1")
    lazy val endpoint = AwsHost("dynamodb.eu-west-1.amazonaws.com")
  }

  /** EU (Frankfurt) */
  case object EU_CENTRAL_1 extends AwsRegion {
    lazy val name = AwsRegionName("eu-central-1")
    lazy val endpoint = AwsHost("dynamodb.eu-central-1.amazonaws.com")
  }

  /** Asia Pacific (Mumbai) */
  case object AP_SOUTH_1 extends AwsRegion {
    lazy val name = AwsRegionName("ap-south-1")
    lazy val endpoint = AwsHost("dynamodb.ap-south-1.amazonaws.com")
  }

  /** Asia Pacific (Singapore) */
  case object AP_SOUTHEAST_1 extends AwsRegion {
    lazy val name = AwsRegionName("ap-southeast-1")
    lazy val endpoint = AwsHost("dynamodb.ap-southeast-1.amazonaws.com")
  }

  /** Asia Pacific (Sydney)	 */
  case object AP_SOUTHEAST_2 extends AwsRegion {
    lazy val name = AwsRegionName("ap-southeast-2")
    lazy val endpoint = AwsHost("dynamodb.ap-southeast-2.amazonaws.com")
  }

  /** Asia Pacific (Tokyo) */
  case object AP_NORTHEAST_1 extends AwsRegion {
    lazy val name = AwsRegionName("ap-northeast-1")
    lazy val endpoint = AwsHost("dynamodb.ap-northeast-1.amazonaws.com")
  }

  /** Asia Pacific (Seoul) */
  case object AP_NORTHEAST_2 extends AwsRegion {
    lazy val name = AwsRegionName("ap-northeast-2")
    lazy val endpoint = AwsHost("dynamodb.ap-northeast-2.amazonaws.com")
  }

  /** South America (São Paulo) */
  case object SA_EAST_1 extends AwsRegion {
    lazy val name = AwsRegionName("sa-east-1")
    lazy val endpoint = AwsHost("dynamodb.sa-east-1.amazonaws.com")
  }

}
