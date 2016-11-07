package org.dynamite.action.put

import org.dynamite.dsl.{AwsCredentials, ClientConfiguration, PutItemError}
import org.dynamite.http.AmazonTargetHeader
import org.dynamite.http.AwsClient._

import scala.concurrent.{ExecutionContext, Future}

object PutItemAction {

  private lazy val PutTargetHeader = AmazonTargetHeader("DynamoDB_20120810.PutItem")

  def put[A](
    configuration: ClientConfiguration,
    credentials: AwsCredentials,
    item: A
  )(implicit ec: ExecutionContext):
  Future[Either[PutItemError, PutItemResult]] = {
    post[PutItemRequest[A], PutItemResponse, PutItemResult, PutItemError](
      PutItemRequest(
        item = item,
        table = configuration.table),
      configuration.awsRegion,
      credentials,
      PutTargetHeader
    )(responseToResult)
  }

  private def responseToResult(putItemResponse: PutItemResponse): PutItemResult = {
    PutItemResult()
  }
}
