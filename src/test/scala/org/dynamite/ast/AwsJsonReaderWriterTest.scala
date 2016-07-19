package org.dynamite.ast

import org.dynamite.Arbitraries._
import org.json4s.JsonAST.JObject
import org.json4s.jackson.JsonMethods._
import org.specs2._

class AwsJsonReaderWriterTest extends Specification with ScalaCheck { def is = s2"""
  The specifications to ensure that AwsJsonReader.fromAws and AwsJsonWriter.toAws are correct
    Augmenting some json to AWS format and shrinking it back should yield the original json $jsonToAwsFromAws
 """

  def jsonToAwsFromAws = prop { jsonObject: JObject =>
    val json = compact(render(jsonObject))
    val transformedJson = compact(render(Dummy.fromAws(Dummy.toAws(jsonObject))))
    transformedJson must be_==(json)
  }.set(minTestsOk = 10000, workers = 10)

  private[this] object Dummy extends AwsJsonReader with AwsJsonWriter
}
