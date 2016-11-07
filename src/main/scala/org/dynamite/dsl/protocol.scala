package org.dynamite.dsl

import org.dynamite.action.get.{GetItemRequest, GetItemResponse}
import org.dynamite.action.put.{GetItemResult, PutItemRequest, PutItemResponse, PutItemResult}
import org.dynamite.ast.AwsTypeSerializer
import org.json4s.DefaultFormats

private[dynamite] trait DynamoProtocol[REQUEST, RESPONSE, RESULT, ERR >: DynamoCommonError] {

  def toErrors: PartialFunction[AwsError, ERR] = toErrorsSpecific orElse toErrorsDefault

  private val toErrorsDefault: PartialFunction[AwsError, ERR] = {
    case e =>
      UnrecognizedAwsError(s"The error $e occurred by was not expected.")
  }

  protected val toErrorsSpecific: PartialFunction[AwsError, ERR]
}

private[dynamite] object DynamoProtocol {
  implicit val formats = DefaultFormats + new AwsTypeSerializer

  implicit def GetItemProtocol[A] = new DynamoProtocol[GetItemRequest, GetItemResponse, GetItemResult[A], GetItemError] {

    val toErrorsSpecific: PartialFunction[AwsError, GetItemError] = {
      case e: GetItemError => e
    }
  }

  implicit def PutItemProtocol[A] = new DynamoProtocol[PutItemRequest[A], PutItemResponse, PutItemResult, PutItemError] {
    val toErrorsSpecific: PartialFunction[AwsError, PutItemError] = {
      case e: PutItemError => e
    }
  }
}