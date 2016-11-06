package org.dynamite

import org.dynamite.ast.{AwsJsonReader, AwsScalarType, AwsTypeSerializer}
import org.dynamite.dsl.{GetItemError, GetItemRequest, _}
import org.dynamite.http.AwsClient.post
import org.dynamite.http._
import org.json4s.DefaultFormats

import scala.concurrent.{ExecutionContext, Future}

/**
  * The high-level interface to query DynamoDB
  */
trait DynamoClient {

  /**
    * Fetch a single item from DynamoDB. The most basic usage looks like :
    *
    * {{{
    * case class Student(name: String)
    * client.get[Student]("id" -> S("studentId1")) //yields Future[Either[DynamoError, GetItemResult[Student]]]
    * }}}
    *
    * @param primaryKey     The primary key to identify the record to fetch
    * @param sortKey        The sort key, to be provided only if a sort key has been specified during table creation
    * @param consistentRead Set to true for consistent read, false for eventual consistent read
    * @tparam A The type of the item to fetch
    * @return The result of the operation, in a plain Scala Future. If everything goes fine,
    *         the disjunction is right-based and return the result of the operation,
    *         i.e. the item to fetch as an Option. Otherwise the disjunction is left-based
    *         and return a meaningful error of what went wrong.
    */
  def get[A](
    primaryKey: (String, AwsScalarType),
    sortKey: Option[(String, AwsScalarType)],
    consistentRead: Boolean)(implicit m: Manifest[A]):
  Future[Either[GetItemError, GetItemResult[A]]]

  /**
    * Put a single item in DynamoDB. The put semantic is intended to be the one from HTTP i.e.
    * it will create an item if it does not exist (identified by the Primary Key + Sort Key)
    * or replace the existing one. Here is an example on how to use it :
    *
    * {{{
    * case class Student(name: String)
    * client.put[Student](Student("John Doe")) //yields Future[Either[DynamoError, PutItemResult]]
    * }}}
    *
    * @param item The item to store in DynamoDB
    * @tparam A The type of the item to store
    * @return The result of the put operation represented as a disjunction in a plain Scala Future.
    *         If the operation completes without error, the disjunction will be right based and will contain
    *         the actual result of the operation. The disjunction will be left based otherwise and will contain
    *         a meaningful error of what went wrong.
    */
  def put[A](item: A)(implicit m: Manifest[A]): Future[Either[PutItemError, PutItemResult]]
}

/**
  * The main implementation to query DynamoDB. A client is dedicated to a table.
  * It can be instantiated as follow :
  *
  * {{{
  * val configuration = ClientConfiguration(AwsTable("students"), AwsRegion.EU_WEST_1)
  * val credentials = AwsCredentials(AwsAccessKey("awsAccessKey"), AwsSecretKey("awsSecretKey"))
  * val client = DynamiteClient(configuration, credentials)
  * }}}
  *
  * @param configuration The configuration of the client, to be able to query
  *                      the right database in the right region.
  * @param credentials   The credentials to authenticate and authorize the actions
  *                      done through the client.
  * @param ec            The execution context the queries are going to be run in.
  *                      It is advised to have a dedicated execution context for
  *                      more control over the performances of the client.
  */
case class DynamiteClient(
  configuration: ClientConfiguration,
  credentials: AwsCredentials)(implicit ec: ExecutionContext)
  extends DynamoClient {

  implicit private val formats = DefaultFormats + new AwsTypeSerializer

  override def get[A](
    primaryKey: (String, AwsScalarType),
    sortKey: Option[(String, AwsScalarType)] = None,
    consistentRead: Boolean = false)(implicit m: Manifest[A]):
  Future[Either[GetItemError, GetItemResult[A]]] = {
    post[GetItemRequest, GetItemResponse, GetItemResult[A], GetItemError](
      GetItemRequest(
        key = (Some(primaryKey) :: sortKey :: Nil).flatten,
        table = configuration.table,
        consistentRead = consistentRead),
      configuration.awsRegion,
      credentials,
      AmazonTargetHeader("DynamoDB_20120810.GetItem")
    ) { res: GetItemResponse =>
      GetItemResult[A](AwsJsonReader.fromAws(res.item).extractOpt[A])
    }
  }

  override def put[A](item: A)(implicit m: Manifest[A]):
  Future[Either[PutItemError, PutItemResult]] = {
    post[PutItemRequest[A], PutItemResponse, PutItemResult, PutItemError](
      PutItemRequest(
        item = item,
        table = configuration.table),
      configuration.awsRegion,
      credentials,
      AmazonTargetHeader("DynamoDB_20120810.PutItem")
    ) { res: PutItemResponse =>
      PutItemResult()
    }
  }
}