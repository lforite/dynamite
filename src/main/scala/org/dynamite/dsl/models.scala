package org.dynamite.dsl

case class AwsCredentials(accessKey: AwsAccessKey, secretKey: AwsSecretKey)

case class AwsAccessKey(value: String)

case class AwsSecretKey(value: String)

case class DateStamp(value: String)

case class AwsRegion(value: String)

case class AwsService(value: String)

case class ClientConfiguration(host: String, table: String)

case class AwsAuthorization(credential: String, signature: String)
