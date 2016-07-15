package org.dynamite.http.auth

import org.dynamite.crypto.{HashFunctions, HexFormatter}
import org.dynamite.dsl.{AwsAuthorization, AwsCredentials, AwsSecretKey, _}
import org.dynamite.http.HttpHeader

import scalaz.Scalaz._
import scalaz.\/

trait AwsRequestSigner
  extends HexFormatter
    with HashFunctions {
  protected[dynamite] def sign(
    credentials: AwsCredentials,
    dateStamp: DateStamp,
    region: AwsRegion,
    service: AwsService): SigningError \/ AwsAuthorization =
    for {
      credential <- s"${credentials.accessKey.value}/${dateStamp.value}/${region.value}/${service.value}/aws4_request".right
      signature <- encode(credentials.secretKey, dateStamp, region, service)
    } yield AwsAuthorization(credential, signature)

  private def encode(
    key: AwsSecretKey,
    dateStamp: DateStamp,
    region: AwsRegion,
    service: AwsService): SigningError \/ String =
    for {
      kSecret <- ("AWS4" + key.value).getBytes("UTF8").right
      kDate <- hmacSha256(dateStamp.value, kSecret)
      kRegion <- hmacSha256(region.value, kDate)
      kService <- hmacSha256(service.value, kRegion)
      result <- hmacSha256("aws4_request", kService) map toHexFormat
    } yield result
}

/** AWS Signature V4 first part of the protocol  more details at
  * http://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html */
trait AwsCanonicalRequestCreator
  extends HexFormatter
    with HashFunctions {

  /** Based on the request parameters, create the canonical request */
  def toCanonicalRequest(
    httpMethod: String,
    uri: String,
    queryParameters: List[(String, List[String])],
    headers: List[HttpHeader],
    requestBody: String): SigningError \/ String = {
    for {
      canonicalHttpMethod <- httpMethod.right
      canonicalUri <- toCanonicalUri(uri).right
      canonicalQueryParameters <- toCanonicalQueryParameters(queryParameters).right
      canonicalHeaders <- toCanonicalHeaders(headers).right
      canonicalSignedHeader <- toCanonicalSignedHeaders(headers).right
      hashedRequestBody <- sha256(requestBody) map toHexFormat map (_.toLowerCase)
    } yield canonicalHttpMethod + "\n" +
      canonicalUri + "\n" +
      canonicalQueryParameters + "\n" +
      canonicalHeaders + "\n" +
      canonicalSignedHeader + "\n" +
      hashedRequestBody
  }

  private def toCanonicalUri(uri: String): String = uri

  private def toCanonicalQueryParameters(queryParameters: List[(String, List[String])]): String = {
    (queryParameters map { qp =>
      qp._2.map(v => qp._1 + "=" + v).mkString("&")
    } sorted) mkString "&"
  }

  private def toCanonicalHeaders(headers: List[HttpHeader]): String = {
    (headers map {
      _.render
    } map { kv =>
      kv._1.toLowerCase + ":" + kv._2.trim + "\n"
    } sorted).foldLeft("") { (acc, s) =>
      acc + s
    }
  }

  private def toCanonicalSignedHeaders(headers: List[HttpHeader]): String = {
    (headers map {
      _.render._1.toLowerCase
    } sorted) mkString ";"
  }
}
