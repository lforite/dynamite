package org.dynamite.action.get

import dynamo.ast.DynamoScalarType
import org.dynamite.action.put.GetItemResult
import org.dynamite.ast.AwsJsonReader
import org.dynamite.dsl.Format._
import org.dynamite.dsl.{AwsCredentials, ClientConfiguration, GetItemError}
import org.dynamite.http.{AmazonTargetHeader, AwsClient}

import scala.concurrent.{ExecutionContext, Future}

object GetItemAction {

  private lazy val GetTargetHeader = AmazonTargetHeader("DynamoDB_20120810.GetItem")

  def get[A](
    configuration: ClientConfiguration,
    credentials: AwsCredentials,
    primaryKey: (String, DynamoScalarType),
    sortKey: Option[(String, DynamoScalarType)] = None,
    consistentRead: Boolean = false
  )(implicit ec: ExecutionContext, m: Manifest[A]):
  Future[Either[GetItemError, GetItemResult[A]]] = {
    AwsClient.post[GetItemRequest, GetItemResponse, GetItemResult[A], GetItemError](
      GetItemRequest(
        key = (Some(primaryKey) :: sortKey :: Nil).flatten,
        table = configuration.table,
        consistentRead = consistentRead),
      configuration.awsRegion,
      credentials,
      GetTargetHeader
    )(responseToResult)
  }

  private def responseToResult[A](res: GetItemResponse)(implicit m: Manifest[A]): GetItemResult[A] = {
    GetItemResult[A](AwsJsonReader.fromAws(res.item).extractOpt[A])
  }
}
