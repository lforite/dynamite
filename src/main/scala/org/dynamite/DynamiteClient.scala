package org.dynamite

import java.time.{ZoneOffset, ZonedDateTime}

import org.dynamite.ast.{AwsJsonReader, AwsScalarType, AwsTypeSerializer}
import org.dynamite.dsl.{GetItemRequest, _}
import org.dynamite.http._
import org.dynamite.http.auth.AwsRequestSigner
import org.json4s.DefaultFormats

import scala.concurrent.{ExecutionContext, Future}
import scalaz.EitherT
import scalaz.Scalaz._

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
  Future[Either[DynamoError, GetItemResult[A]]]
}

/**
  * The main implementation to query DynamoDB. A client is dedicated to a table.
  * It can be instantiated as follow :
  *
  * {{{
  * val  configuration = ClientConfiguration(AwsTable("students"), AwsRegion.EU_WEST_1)
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
  Future[Either[DynamoError, GetItemResult[A]]] = {
    requestAws(
      GetItemRequest(
        key = (Some(primaryKey) :: sortKey :: Nil).flatten,
        table = configuration.table,
        consistentRead = consistentRead),
      AmazonTargetHeader("DynamoDB_20120810.GetItem")
    ) { res: GetItemResponse =>
      GetItemResult[A](AwsJsonReader.fromAws(res.item).extractOpt[A])
    }
  }

  private def requestAws[REQUEST: JsonSerializable, RESPONSE: JsonDeserializable, RESULT](
    request: REQUEST,
    targetHeader: AmazonTargetHeader)
    (respToRes: RESPONSE => RESULT)
    (implicit
      ec: ExecutionContext,
      protocol: DynamoProtocol[REQUEST, RESPONSE, RESULT]):
  Future[Either[DynamoError, RESULT]] = {
    EitherT.fromDisjunction[Future] {
      for {
        awsHost <- configuration.awsRegion.endpoint.right
        dateStamp <- AwsDate(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime).right
        headers <- (
          AcceptEncodingHeader("identity") ::
            ContentTypeHeader("application/x-amz-json-1.0") ::
            AmazonDateHeader(dateStamp.dateTime) ::
            HostHeader(awsHost) ::
            targetHeader ::
            Nil).right
        requestBody <- JsonSerializable[REQUEST].serialize(request)
        signingHeaders <- AwsRequestSigner.signRequest(
          httpMethod = HttpMethod.POST,
          uri = Uri("/"),
          queryParameters = List(),
          headers = headers,
          requestBody = requestBody,
          awsDate = dateStamp,
          awsRegion = configuration.awsRegion,
          awsService = AwsService("dynamodb"),
          awsCredentials = credentials)
        signedHeaders <- (AuthorizationHeader(signingHeaders) :: headers).right
      } yield AwsHttpRequest(awsHost, requestBody, signedHeaders)
    } flatMap {
      HttpClient.httpRequest
    } flatMapF { res =>
      Future {
        for {
          json <- RequestParser.parse(res.responseBody.value)
          response <- JsonDeserializable[RESPONSE].deserialize(json).right
          result <- respToRes(response).right
        } yield result
      }
    } toEither
  }
}