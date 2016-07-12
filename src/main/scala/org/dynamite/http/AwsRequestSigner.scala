package org.dynamite.http

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.dynamite.dsl.{AwsAuthorization, AwsCredentials, AwsSecretKey, _}

import scalaz.Scalaz._
import scalaz.\/

trait AwsRequestSigner {
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
      kDate <- hmacSHA256(dateStamp.value, kSecret)
      kRegion <- hmacSHA256(region.value, kDate)
      kService <- hmacSHA256(service.value, kRegion)
      result <- hmacSHA256("aws4_request", kService).map(toHexFormat)
    } yield result

  private def hmacSHA256(data: String, key: Array[Byte]): SigningError \/ Array[Byte] =
    (for {
      algorithm <- "HmacSHA256".right
      mac <- \/.fromTryCatchThrowable[Mac, Throwable](Mac.getInstance(algorithm))
      _ <- \/.fromTryCatchThrowable[Unit, Throwable](mac.init(new SecretKeySpec(key, algorithm)))
      result <- \/.fromTryCatchThrowable[Array[Byte], Throwable](mac.doFinal(data.getBytes("UTF8")))
    } yield result).leftMap(t => SigningError("Oooooppsssss something wrong occurred"))

  private def toHexFormat(bytes: Array[Byte]) = bytes.map("%02x" format _).mkString
}
