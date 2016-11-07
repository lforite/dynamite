package org.dynamite.dsl

import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JField, JObject, JString}

sealed trait GetItemError
sealed trait PutItemError

/** more info at http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/CommonErrors.html */
sealed trait DynamoCommonError extends GetItemError with PutItemError
case class UnreachableHostError(host: String) extends DynamoCommonError
case class InvalidHostError(host: String) extends DynamoCommonError
case class UnexpectedDynamoError(message: String) extends DynamoCommonError
case object JsonSerialisationError extends DynamoCommonError
case class JsonDeserialisationError(message: String) extends DynamoCommonError
case class SigningError(error: String) extends DynamoCommonError

/**
  * Your request rate is too high. Reduce the frequency of
  * requests and use exponential backoff. For more information, go to <a href=
  * "http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ErrorHandling.html#APIRetries"
  * >Error Retries and Exponential Backoff</a> in the <i>Amazon DynamoDB
  * Developer Guide</i>.
  *
  * @param description A detailed description of what went wrong
  */
case class ProvisionedThroughputExceededError(description: String)
  extends AwsError
    with GetItemError
    with PutItemError

/**
  * The operation tried to access a nonexistent table or index. The
  * resource might not be specified correctly, or its status might
  * not be <code>ACTIVE</code>.
  *
  * @param description A detailed description of what went wrong
  */
case class ResourceNotFoundError(description: String)
  extends AwsError
    with GetItemError
    with PutItemError

/**
  * An internal error occurred on DynamoDB side. You might retry your request.
  *
  * @param description A detailed description of what went wrong
  */
case class InternalServerError(description: String)
  extends AwsError
    with GetItemError
    with PutItemError

/**
  * DynamoDB is currently unavailable. (This should be a temporary state.). You might retry your request.
  *
  * @param description A detailed description of what went wrong
  */
case class ServiceUnavailableError(description: String)
  extends AwsError
    with GetItemError
    with PutItemError


case class InvalidCredentialsError(description: String)
  extends AwsError
    with GetItemError
    with PutItemError

/**
  * This error should not happen, it would indicate a that a case has not been covered with the parsing of DynamoDB error
  *
  * @param description A detailed description of what went wrong
  */
case class UnrecognizedAwsError(description: String)
  extends AwsError
    with DynamoCommonError


/**
  * A condition specified in the operation could not be evaluated.
  *
  * @param description A detailed description of what went wrong
  */
case class ConditionalCheckFailedError(description: String)
  extends AwsError
    with PutItemError

/**
  * An item collection is too large. This exception is only returned for tables
  * that have one or more local secondary indexes.
  *
  * @param description A detailed description of what went wrong
  */
case class ItemCollectionSizeLimitExceededError(description: String)
  extends AwsError
    with PutItemError

trait AwsError {
  val description: String
}

object AwsError {
  def apply(errorType: String, description: String):AwsError = {
    errorType.split("#").last match {
      case "MissingAuthenticationTokenException" | "UnrecognizedClientException" => InvalidCredentialsError(description)
      case "ProvisionedThroughputExceededException" => ProvisionedThroughputExceededError(description)
      case "ResourceNotFoundException" => ResourceNotFoundError(description)
      case "InternalServerException" => InternalServerError(description)
      case "ConditionalCheckFailedException" => ConditionalCheckFailedError(description)
      case "ItemCollectionSizeLimitExceededException" => ItemCollectionSizeLimitExceededError(description)
      case _ => UnrecognizedAwsError(description)
    }
  }
}

private[dynamite] class AwsErrorSerializer extends CustomSerializer[AwsError](format => ( {
  case JObject(List(JField("__type", JString(value)), JField("message", JString(description)))) => AwsError(value, description)
  case e =>
    //todo: add some logging here
    UnrecognizedAwsError("Dynamite was not able to understand the resp onse from DynamoDB")
},
  //We are not interested in serialising those errors so this is a Dummy place holder
  {
    case _ => JObject()
  }))