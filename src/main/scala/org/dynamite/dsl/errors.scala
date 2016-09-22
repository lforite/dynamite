package org.dynamite.dsl

import javax.crypto.spec.SecretKeySpec

import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JField, JObject, JString}

sealed trait GetItemError

/** more info at http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/CommonErrors.html */
sealed trait DynamoCommonError extends GetItemError

case class BasicDynamoError() extends DynamoCommonError
case class UnreachableHostError(host: String) extends DynamoCommonError
case class InvalidHostError(host: String) extends DynamoCommonError
case class UnexpectedDynamoError(message: String) extends DynamoCommonError
case object JsonSerialisationError extends DynamoCommonError

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

trait AwsError {
  val description: String
}

private[dynamite] class AwsErrorSerializer extends CustomSerializer[AwsError](format => ( {
  case JObject(List(JField("__type", JString(value)), JField("message", JString(description)))) => AwsError.test(value, description)
  case e => InternalServerError("dedeeqeqeq")
}, { case _ => JString("")}))

object AwsError {
  def test(errorType: String, description: String):AwsError = {
    errorType.split("#").last match {
        //might occur anytime
//      case "AccessDeniedException" => "The client did not correctly sign the request."
  //    case "MissingAuthenticationTokenException" => "Request must contain a valid (registered) AWS Access Key ID."
    //  case "IncompleteSignatureException" => "The request signature did not include all of the required components."


        //should happen
      //case "ConditionalCheckFailedException" => "You specified a condition that evaluated to false."//might happen
      //case "ItemCollectionSizeLimitExceededException" => "Collection size exceeded." //might happen
      //case "LimitExceededException" => "Too many operations for a given subscriber." //might happen
      case "ProvisionedThroughputExceededException" => ProvisionedThroughputExceededError(description)
      //case "ResourceInUseException" => "The resource which you are attempting to change is in use." //might happen
      case "ResourceNotFoundException" => ResourceNotFoundError(description)
      //case "ThrottlingException" => "Rate of requests exceeds the allowed throughput." //might happen
      //case "UnrecognizedClientException" => "The Access Key ID or security token is invalid."   //might happen
      //case "ValidationException" => "Varies, depending upon the specific error(s) encountered" //might happen
      case "InternalServerException" => InternalServerError(description)
    }
  }
}

sealed trait HashingError
case class EncodingNotFoundError(encoding: String) extends HashingError
case class AlgorithmNotFoundError(algorithm: String) extends HashingError
case class InvalidSecretKeyError(key: SecretKeySpec) extends HashingError
case object NotInitializedMacError extends HashingError