package org.dynamite.http

import java.io.{IOException, InputStream}
import java.net.URI
import java.nio.ByteBuffer
import java.util

import com.ning.http.client.cookie.Cookie
import com.ning.http.client.{FluentCaseInsensitiveStringsMap, Response}
import org.dynamite.dsl.BasicDynamoError
import org.specs2.Specification

class RequestExtractorTest extends Specification { def is = s2"""
      Specifications for the request extractor
        Extracting a response body with no exception should yield the json as a String $extractOk
        Extracting a response body with exception should yield an error $extractKo
    """

  private def extractOk = {
    Dummy.extract(new ResponseMock("{}")) fold(
      err => ko("It should be a success"),
      succ => succ should be_==("{}")
      )
  }

  private def extractKo = {
    Dummy.extract(new ResponseMock(new IOException("IOException occurred"))) fold(
      err => err should be_==(BasicDynamoError()),
      succ => ko("It should be an error")
      )
  }

  private[this] object Dummy extends RequestExtractor

  private[this] class ResponseMock(returnValue: Any) extends Response {

    override def getResponseBody: String = returnValue match {
      case s: String => s
      case e: IOException => throw e
      case _ => ""
    }

    override def getResponseBodyExcerpt(maxLength: Int, charset: String): String = ???
    override def getResponseBodyExcerpt(maxLength: Int): String = ???
    override def getResponseBodyAsByteBuffer: ByteBuffer = ???
    override def getStatusCode: Int = ???
    override def getResponseBodyAsBytes: Array[Byte] = ???
    override def getResponseBodyAsStream: InputStream = ???
    override def isRedirected: Boolean = ???
    override def getCookies: util.List[Cookie] = ???
    override def hasResponseBody: Boolean = ???
    override def getStatusText: String = ???
    override def getHeaders(name: String): util.List[String] = ???
    override def getHeaders: FluentCaseInsensitiveStringsMap = ???
    override def hasResponseHeaders: Boolean = ???
    override def getResponseBody(charset: String): String = ???
    override def getContentType: String = ???
    override def hasResponseStatus: Boolean = ???
    override def getUri: URI = ???
    override def getHeader(name: String): String = ???
  }

}
