package org.dynamite.dsl

case class AwsCredentials(accessKey: String, secretKey: String)

case class ClientConfiguration(host: String, table: String)

case class AwsAuthorization(credential: String, signature: String)
