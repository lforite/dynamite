package org.dynamite.action.delete

import org.dynamite.ast.AwsScalarType
import org.dynamite.dsl.{AwsCredentials, ClientConfiguration, DeleteItemError}
import org.dynamite.http.{AmazonTargetHeader, AwsClient}

import scala.concurrent.{ExecutionContext, Future}

object DeleteItemAction {

  lazy val DeleteTargetHeader = AmazonTargetHeader("DynamoDB_20120810.DeleteItem ")

  def delete(
    configuration: ClientConfiguration,
    credentials: AwsCredentials,
    primaryKey: (String, AwsScalarType),
    sortKey: Option[(String, AwsScalarType)]
  )(implicit ec: ExecutionContext): Future[Either[DeleteItemError, DeleteItemResult]] = {
    AwsClient.post[DeleteItemRequest, DeleteItemResponse, DeleteItemResult, DeleteItemError](
      DeleteItemRequest(
        key = (Some(primaryKey) :: sortKey :: Nil).flatten,
        table = configuration.table
      ),
      configuration.awsRegion,
      credentials,
      DeleteTargetHeader
    )(_ => DeleteItemResult())
  }

//  private def toResult(response: DeleteItemResponse): DeleteItemResult = {
//
//  }
}
