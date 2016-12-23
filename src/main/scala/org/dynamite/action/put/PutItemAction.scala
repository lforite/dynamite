package org.dynamite.action.put

import dynamo.ast.writes.DynamoWrite
import org.dynamite.dsl.{AwsCredentials, ClientConfiguration, PutItemError}
import org.dynamite.http.AmazonTargetHeader
import org.dynamite.http.AwsClient._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{\/, \/-}

object PutItemAction {

  private lazy val PutTargetHeader = AmazonTargetHeader("DynamoDB_20120810.PutItem")

  def put[A](
    configuration: ClientConfiguration,
    credentials: AwsCredentials,
    item: A
  )(implicit ec: ExecutionContext, m: DynamoWrite[A]):
  Future[Either[PutItemError, PutItemResult]] = {
    post[PutItemRequest[A], PutItemResponse, PutItemResult, PutItemError](
      PutItemRequest(
        item = m.write(item),
        table = configuration.table),
      configuration.awsRegion,
      credentials,
      PutTargetHeader
    )(responseToResult)
  }

  private def responseToResult(putItemResponse: PutItemResponse): PutItemError \/ PutItemResult = {
    \/-(PutItemResult())
  }
}
