package org.dynamite.ast

import dynamo.ast.DynamoType
import org.dynamite.ast.AwsTypesArbitraries._
import org.json4s.jackson.Serialization.{read, write}
import org.specs2.{ScalaCheck, Specification}

class AwsTypeSerialiserDeserialiserTest extends Specification with ScalaCheck { override def is = s2"""
      Serialise and then deserialise any AwsType should yield the original value $serialiseDeserialise
    """

  import org.dynamite.dsl.Format.defaultFormats

  def serialiseDeserialise = prop { awsType: DynamoType =>
    read[DynamoType](write(awsType)) must_== awsType
  }.set(minTestsOk = 20000, workers = 10)

}
