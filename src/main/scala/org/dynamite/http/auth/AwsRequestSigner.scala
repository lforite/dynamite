package org.dynamite.http.auth

import org.dynamite.crypto.{HashFunctions, HexFormatter}
import org.dynamite.dsl.{AwsCredentials, AwsSecretKey, AwsSigningHeaders, _}
import org.dynamite.http.HttpHeader

import scalaz.Scalaz._
import scalaz.\/

/** AWS Signature V4 first part of the signing protocol; more details at
  * http://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html */
private[dynamite] object AwsCanonicalRequestBuilder {
  /** Based on the request parameters, create the canonical request */
  def canonicalRequest(
    httpMethod: HttpMethod,
    uri: Uri,
    queryParameters: List[(String, List[String])],
    headers: List[HttpHeader],
    requestBody: RequestBody): SigningError \/ AwsCanonicalRequest = {
    (for {
      canonicalHttpMethod <- httpMethod.value.right
      canonicalUri <- toCanonicalUri(uri).right
      canonicalQueryParameters <- toCanonicalQueryParameters(queryParameters).right
      canonicalHeaders <- toCanonicalHeaders(headers).right
      canonicalSignedHeader <- toCanonicalSignedHeaders(headers).right
      hashedRequestBody <- HashFunctions.sha256(requestBody.value) map HexFormatter.toHexFormat map (_.toLowerCase)
    } yield AwsCanonicalRequest(
      canonicalHttpMethod + "\n" +
        canonicalUri + "\n" +
        canonicalQueryParameters + "\n" +
        canonicalHeaders + "\n" +
        canonicalSignedHeader.value + "\n" +
        hashedRequestBody,
      canonicalSignedHeader)) leftMap (he => SigningError(s"Unexpected error occurred while signing the request, $he"))
  }

  private def toCanonicalUri(uri: Uri): String = uri.value

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

  private def toCanonicalSignedHeaders(headers: List[HttpHeader]): AwsSignedHeaders =
    AwsSignedHeaders(
      (headers map {
        _.render._1.toLowerCase
      } sorted) mkString ";")
}

/** AWS Signature V4 second part of the signing protocol; more details at
  * http://docs.aws.amazon.com/general/latest/gr/sigv4-create-string-to-sign.html */
private[dynamite] object AwsStringToSignBuilder {

  def stringToSign(
    awsDate: AwsDate,
    region: AwsRegion,
    service: AwsService,
    canonicalRequest: AwsCanonicalRequest): SigningError \/ AwsStringToSign = {
    (for {
      algorithm <- "AWS4-HMAC-SHA256".right
      date <- awsDate.dateTime.value.right
      scope <- s"${awsDate.date.value}/${region.name.value}/${service.value}/aws4_request".right
      hashedCanonicalRequest <- HashFunctions.sha256(canonicalRequest.value) map HexFormatter.toHexFormat
    } yield AwsStringToSign(
      algorithm + '\n' +
        date + '\n' +
        scope + '\n' +
        hashedCanonicalRequest,
      AwsScope(scope))) leftMap (he => SigningError(s"Unexpected error occurred while signing the request, $he"))
  }

}

/** AWS Signature V4 third part of the signing protocol; more details at
  * http://docs.aws.amazon.com/general/latest/gr/sigv4-create-string-to-sign.html */
private[dynamite] object AwsSigningKeyBuilder {
  def derive(
    credentials: AwsCredentials,
    dateStamp: DateStamp,
    region: AwsRegion,
    service: AwsService): SigningError \/ AwsSigningKey =
    for {
      signature <- encode(credentials.secretKey, dateStamp, region, service)
    } yield AwsSigningKey(signature)

  private def encode(
    key: AwsSecretKey,
    dateStamp: DateStamp,
    region: AwsRegion,
    service: AwsService): SigningError \/ Array[Byte] =
    (for {
      kSecret <- ("AWS4" + key.value).getBytes("UTF-8").right
      kDate <- HashFunctions.hmacSha256(dateStamp.value, kSecret)
      kRegion <- HashFunctions.hmacSha256(region.name.value, kDate)
      kService <- HashFunctions.hmacSha256(service.value, kRegion)
      result <- HashFunctions.hmacSha256("aws4_request", kService)
    } yield result) leftMap (he => SigningError(s"Unexpected error occurred while signing the request, $he"))
}

/** AWS Signature V4 final step of the signing protocol */
private[dynamite] object AwsSignatureBuilder {
  def sign(
    signingKey: AwsSigningKey,
    stringToSign: AwsStringToSign): SigningError \/ AwsSignature = {
    (for {
      signature <- HashFunctions.hmacSha256(stringToSign.value, signingKey.value) map HexFormatter.toHexFormat
    } yield AwsSignature(signature)) leftMap (he => SigningError(s"Unexpected error occurred while signing the request, $he"))
  }
}

/** The component putting together */
private[dynamite] object AwsRequestSigner {
  def signRequest(
    httpMethod: HttpMethod,
    uri: Uri,
    queryParameters: List[(String, List[String])],
    headers: List[HttpHeader],
    requestBody: RequestBody,
    awsDate: AwsDate,
    awsRegion: AwsRegion,
    awsService: AwsService,
    awsCredentials: AwsCredentials): SigningError \/ AwsSigningHeaders = {
    for {
      cRequest <- AwsCanonicalRequestBuilder.canonicalRequest(httpMethod, uri, queryParameters, headers, requestBody)
      sToS <- AwsStringToSignBuilder.stringToSign(awsDate, awsRegion, awsService, cRequest)
      signingKey <- AwsSigningKeyBuilder.derive(awsCredentials, awsDate.date, awsRegion, awsService)
      signature <- AwsSignatureBuilder.sign(signingKey, sToS)
    } yield AwsSigningHeaders(
      AwsSigningCredentials(awsCredentials.accessKey.value + "/" + sToS.scope.value),
      cRequest.signedHeaders,
      signature)
  }
}