package org.dynamite

/** more info at http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/CommonErrors.html */
sealed trait DynamoError
case class BasicDynamoError() extends DynamoError
case object IncompleteSignatureError extends DynamoError
case object InternalFailureError extends DynamoError
case object InvalidActionError extends DynamoError
case object InvalidClientTokenId extends DynamoError
case object InvalidParameterCombination extends DynamoError
case object InvalidParameterValue extends DynamoError
case object InvalidQueryParameter extends DynamoError
case object MalformedQueryString extends DynamoError
case object MissingAction extends DynamoError
case object MissingAuthenticationToken extends DynamoError
case object MissingParameter extends DynamoError
case object OptInRequired extends DynamoError
case object RequestExpired extends DynamoError
case object ServiceUnavailable extends DynamoError
case object Throttling extends DynamoError
case object ValidationError extends DynamoError