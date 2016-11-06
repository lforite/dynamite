package org.dynamite.dsl

import javax.crypto.spec.SecretKeySpec

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

/**
  * An internal error occurred on DynamoDB side. You might retry your request.
  * @param description A detailed description of what went wrong
  */
case class InternalServerError(description: String)
  extends AwsError
    with GetItemError

/**
  * DynamoDB is currently unavailable. (This should be a temporary state.). You might retry your request.
  * @param description A detailed description of what went wrong
  */
case class ServiceUnavailableError(description: String)
  extends AwsError
    with GetItemError


case class InvalidCredentialsError(description: String)
  extends AwsError
    with DynamoCommonError


/**
  * This error should not happen, it would indicate a that a case has not been covered with the parsing of DynamoDB error
  * @param description A detailed description of what went wrong
  */
case class UnrecognizedAwsError(description: String)
  extends AwsError
    with DynamoCommonError

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
      //case "ConditionalCheckFailedException" => "You specified a condition that evaluated to false."//might happen
      //case "ItemCollectionSizeLimitExceededException" => "Collection size exceeded." //might happen
      //case "LimitExceededException" => "Too many operations for a given subscriber." //might happen
      //case "ResourceInUseException" => "The resource which you are attempting to change is in use." //might happen
      //case "ThrottlingException" => "Rate of requests exceeds the allowed throughput." //might happen
      //case "ValidationException" => "Varies, depending upon the specific error(s) encountered" //might happen
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

sealed trait HashingError
case class EncodingNotFoundError(encoding: String) extends HashingError
case class AlgorithmNotFoundError(algorithm: String) extends HashingError
case class InvalidSecretKeyError(key: SecretKeySpec) extends HashingError
case object NotInitializedMacError extends HashingError