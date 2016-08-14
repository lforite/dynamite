package org.dynamite.crypto

import org.specs2.Specification

class HashFunctionsTest extends Specification { override def is = s2"""
      Specifications for the hash functions
        Calling sha256 with an empty string should yield the expected value $sha256EmptyString
        Calling sha256 with an arbitrary string should yield the expected value $sha256ArbitraryString
    """.stripMargin

  //TODO: add UT for hmacSha256

  /** According to http://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html */
  def sha256EmptyString = {
    HashFunctions.sha256("") fold(
      err => ko("The hashing function is expected to work"),
      succ => Dummy.toHexFormat(succ) must be_==("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
      )
  }
  /** According to http://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html */
  def sha256ArbitraryString = {
    HashFunctions.sha256("GET\n/\nAction=ListUsers&Version=2010-05-08\ncontent-type:application/x-www-form-urlencoded; charset=utf-8\nhost:iam.amazonaws.com\nx-amz-date:20150830T123600Z\n\ncontent-type;host;x-amz-date\ne3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855") fold(
      err => ko("The hashing function is expected to work"),
      succ => Dummy.toHexFormat(succ) must be_==("f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59")
      )
  }

  private[this] object Dummy extends HexFormatter

}
