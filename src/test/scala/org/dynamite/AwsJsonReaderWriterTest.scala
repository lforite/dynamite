package org.dynamite

import org.json4s.JsonAST.JObject
import org.specs2._
import Arbitraries._

class AwsJsonReaderWriterTest extends Specification with ScalaCheck { def is = s2"""
  The specifications to ensure that AwsJsonReader.fromAws and AwsJsonWriter.toAws are correct
    Augmenting some json to AWS format and shrinking it back should yield the original json $jsonToAwsFromAws
 """"

  def jsonToAwsFromAws = prop { json: JObject =>
    (Dummy.fromAws _ compose Dummy.toAws _) (json) must be_==(json)
  }

  private[this] object Dummy extends AwsJsonReader with AwsJsonWriter
}
