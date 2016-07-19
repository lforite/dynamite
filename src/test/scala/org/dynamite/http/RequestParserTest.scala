package org.dynamite.http

import org.dynamite.Arbitraries._
import org.dynamite.ValidJson
import org.specs2.{ScalaCheck, Specification}

class RequestParserTest extends Specification with ScalaCheck { def is = s2"""
      Specifications for the request parser
        Parsing a correct json should yield a JValue $parseOk
        Parsing an invalid json should yield an error $parseKo
  """

  def parseOk = prop { validJson: ValidJson =>
    Dummy.parse(validJson.json) fold(
      left => ko(validJson.json + "should be valid"),
      right => ok
      )
  }

  def parseKo = prop { invalidJson: String =>
    Dummy.parse(invalidJson) fold(
      left => ok,
      right => ko(invalidJson + "should be invalid")
      )
  }

  private[this] object Dummy extends RequestParser

}
