package org.dynamite.action.get

import dynamo.ast.DynamoScalarType
import dynamo.ast.reads.{DynamoRead, DynamoReadError, DynamoReadSuccess}
import io.circe.Decoder
import org.dynamite.ast.AwsTypeSerialiser._
import org.dynamite.ast.ROOT
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

  private def responseToResult[A: DynamoRead](res: GetItemResponse): GetItemError \/ GetItemResult[A] = {
    val result = res.item match {
      case Some(item) =>
        Decoder[ROOT].decodeJson(item) match {
          case Right(root) =>
            DynamoRead[A].read(root.dynamoType) match {
              case DynamoReadSuccess(a) => \/-(Some(a))
              case DynamoReadError(path, error) => -\/(JsonDeserialisationError(s"""At path $path, got the following error: "$error"."""))
            }
          case Left(error) => \/-(None)
        }
      case None => \/-(None)
    }

    result map GetItemResult[A]
  }
}
