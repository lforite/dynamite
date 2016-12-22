package org.dynamite.action.get

import dynamo.ast.reads.{DynamoRead, DynamoReadError, DynamoReadSuccess}
import dynamo.ast.{DynamoScalarType, DynamoType, M}
import org.dynamite.action.put.GetItemResult
import org.dynamite.dsl.Format._
import org.dynamite.dsl.{AwsCredentials, ClientConfiguration, GetItemError, JsonDeserialisationError}
import org.dynamite.http.{AmazonTargetHeader, AwsClient}

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{-\/, \/, \/-}

object GetItemAction {

  private lazy val GetTargetHeader = AmazonTargetHeader("DynamoDB_20120810.GetItem")

  def get[A](
    configuration: ClientConfiguration,
    credentials: AwsCredentials,
    primaryKey: (String, DynamoScalarType),
    sortKey: Option[(String, DynamoScalarType)] = None,
    consistentRead: Boolean = false
  )(implicit ec: ExecutionContext, m: DynamoRead[A]):
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

  private def responseToResult[A](res: GetItemResponse)(implicit m: DynamoRead[A]): GetItemError \/ GetItemResult[A] = {

    val dynamoTypeOpt: Option[DynamoType] = res.item.extractOpt[Map[String, DynamoType]].map(map => M(map.toList))

    val result: GetItemError \/ Option[A] = dynamoTypeOpt match {
      case Some(dynamoType) =>
        DynamoRead[A].read(dynamoType) match {
          case DynamoReadSuccess(a) => \/-(Some(a))
          case DynamoReadError(path, error) => -\/(JsonDeserialisationError(s"""At path $path, got the following error: "$error"."""))
        }
      case None => \/-(None)
    }

    result map GetItemResult[A]
  }
}
