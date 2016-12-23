package org.dynamite.action.delete

import dynamo.ast.DynamoScalarType
import org.dynamite.dsl.{AwsCredentials, ClientConfiguration, DeleteItemError}
import org.dynamite.http.{AmazonTargetHeader, AwsClient}

import scala.concurrent.{ExecutionContext, Future}
import scalaz.\/-

object DeleteItemAction {

  lazy val DeleteTargetHeader = AmazonTargetHeader("DynamoDB_20120810.DeleteItem ")

  def delete(
    configuration: ClientConfiguration,
    credentials: AwsCredentials,
    primaryKey: (String, DynamoScalarType),
    sortKey: Option[(String, DynamoScalarType)]
  )(implicit ec: ExecutionContext): Future[Either[DeleteItemError, DeleteItemResult]] = {
    AwsClient.post[DeleteItemRequest, DeleteItemResponse, DeleteItemResult, DeleteItemError](
      DeleteItemRequest(
        key = (Some(primaryKey) :: sortKey :: Nil).flatten,
        table = configuration.table
      ),
      configuration.awsRegion,
      credentials,
      DeleteTargetHeader
    )(_ => \/-(DeleteItemResult()))
  }

}
