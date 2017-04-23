package org.dynamite.dsl

import org.dynamite.action.delete.{DeleteItemRequest, DeleteItemResponse, DeleteItemResult}
import org.dynamite.action.get.{GetItemRequest, GetItemResponse, GetItemResult}
import org.dynamite.action.put.{PutItemRequest, PutItemResponse, PutItemResult}

private[dynamite] trait DynamoProtocol[REQUEST, RESPONSE, RESULT, ERR >: DynamoCommonError] {

  def toErrors: PartialFunction[AwsError, ERR] = toErrorsSpecific orElse toErrorsDefault

  private val toErrorsDefault: PartialFunction[AwsError, ERR] = {
    case e =>
      UnrecognizedAwsError(s"The error $e occurred by was not expected.")
  }

  protected val toErrorsSpecific: PartialFunction[AwsError, ERR]
}

private[dynamite] object DynamoProtocol {

  implicit def GetItemProtocol[A] = new DynamoProtocol[GetItemRequest, GetItemResponse, GetItemResult[A], GetItemError] {
    val toErrorsSpecific: PartialFunction[AwsError, GetItemError] = {
      case e: GetItemError => e
    }
  }

  implicit def PutItemProtocol[A] = new DynamoProtocol[PutItemRequest, PutItemResponse, PutItemResult, PutItemError] {
    val toErrorsSpecific: PartialFunction[AwsError, PutItemError] = {
      case e: PutItemError => e
    }
  }

  implicit def DeleteItemProtocol = new DynamoProtocol[DeleteItemRequest, DeleteItemResponse, DeleteItemResult, DeleteItemError] {
    val toErrorsSpecific: PartialFunction[AwsError, DeleteItemError] = {
      case e: DeleteItemError => e
    }
  }
}