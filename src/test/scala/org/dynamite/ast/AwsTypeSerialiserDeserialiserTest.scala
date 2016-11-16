package org.dynamite.ast

import org.json4s.jackson.Serialization.{read, write}
import org.specs2.{ScalaCheck, Specification}
import org.dynamite.ast.AwsTypesArbitraries._

class AwsTypeSerialiserDeserialiserTest extends Specification with ScalaCheck { override def is = s2"""
      Serialise and then deserialise any AwsType should yield the original value $serialiseDeserialise
      Serialise and then deserialise a DynamoDB document $serialiseDeserialiseDynamo
    """

  import org.dynamite.dsl.Format.defaultFormats

  def serialiseDeserialise = prop { awsType: AwsType =>
    read[AwsType](write(awsType)) must_== awsType
  }.set(minTestsOk = 20000, workers = 10)

  def serialiseDeserialiseDynamo = prop { root: ROOT =>
    read[AwsType](write(root)) must_== root
  }.set(minTestsOk = 20000, workers = 10)

}
