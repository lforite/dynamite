package org.dynamite.dsl

case class GetItemResult[A](item: Option[A])

case class PutItemResult(result: Boolean)