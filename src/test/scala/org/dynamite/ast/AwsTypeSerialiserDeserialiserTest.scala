package org.dynamite.ast

import dynamo.ast._
import io.circe.{Decoder, Encoder}
import org.dynamite.ast.AwsTypesArbitraries._
import org.dynamite.ast.AwsTypeSerialiser._
import org.specs2.{ScalaCheck, Specification}

class AwsTypeSerialiserDeserialiserTest extends Specification with ScalaCheck { override def is = s2"""
      Serialise and then deserialise any AwsType should yield the original value $serialiseDeserialise
    """

  def serialiseDeserialise = prop { awsType: DynamoType =>
    Decoder[DynamoType].decodeJson(Encoder[DynamoType].apply(awsType)) must beRight(awsType)
  }.set(minTestsOk = 20000, workers = 10)

}
