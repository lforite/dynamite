package org.dynamite

import dynamo.ast.DynamoScalarType
import dynamo.ast.reads.DynamoRead
import dynamo.ast.writes.DynamoWrite
import org.dynamite.action.delete.{DeleteItemAction, DeleteItemResult}
import org.dynamite.action.get.GetItemAction
import org.dynamite.action.put._
import org.dynamite.dsl._

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
    primaryKey: (String, DynamoScalarType),
    sortKey: Option[(String, DynamoScalarType)],
    consistentRead: Boolean)(implicit m: DynamoRead[A]):
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
  def put[A](item: A)(implicit m: DynamoWrite[A]): Future[Either[PutItemError, PutItemResult]]
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

  override def get[A](
    primaryKey: (String, DynamoScalarType),
    sortKey: Option[(String, DynamoScalarType)] = None,
    consistentRead: Boolean = false)(implicit m: DynamoRead[A]):
  Future[Either[GetItemError, GetItemResult[A]]] = {
    GetItemAction.get(configuration, credentials, primaryKey, sortKey, consistentRead)
  }

  override def put[A](item: A)(implicit m: DynamoWrite[A]):
  Future[Either[PutItemError, PutItemResult]] = {
    PutItemAction.put(configuration, credentials, item)
  }

  def delete(
    primaryKey: (String, DynamoScalarType),
    sortKey: Option[(String, DynamoScalarType)] = None
  ): Future[Either[DeleteItemError, DeleteItemResult]] = {
    DeleteItemAction.delete(configuration, credentials, primaryKey, sortKey)
  }

}
