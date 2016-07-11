package org.dynamite.http

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.dynamite.dsl.{AwsAuthorization, AwsCredentials}

import scalaz.Scalaz._
import scalaz.\/

object AwsRequestSigner {

  def sign(credentials: AwsCredentials, timeStamp: String, region: String, service: String): String \/ AwsAuthorization =
    for {
      credential <- s"${credentials.accessKey}/$timeStamp/$region/dynamo/aws4_request".right
      signature <- getSignatureKey(s"${credentials.secretKey}", timeStamp, region, service)
    } yield AwsAuthorization(credential, signature)

  private def getSignatureKey(key: String, dateStamp: String, regionName: String, serviceName: String): String \/ String =
    for {
      kSecret <- ("AWS4" + key).getBytes("UTF8").right
      kDate <- hmacSHA256(dateStamp, kSecret)
      kRegion <- hmacSHA256(regionName, kDate)
      kService <- hmacSHA256(serviceName, kRegion)
      result <- hmacSHA256("aws4_request", kService).map(toHexFormat)
    } yield result

  private def toHexFormat(bytes: Array[Byte]) = bytes.map("%02x" format _).mkString

  private def hmacSHA256(data: String, key: Array[Byte]): String \/ Array[Byte] =
    (for {
      algorithm <- "HmacSHA256".right
      mac <- \/.fromTryCatchThrowable[Mac, Throwable](Mac.getInstance(algorithm))
      _ <- \/.fromTryCatchThrowable[Unit, Throwable](mac.init(new SecretKeySpec(key, algorithm)))
      result <- \/.fromTryCatchThrowable[Array[Byte], Throwable](mac.doFinal(data.getBytes("UTF8")))
    } yield result).leftMap(t => "Oooooppsssss something wrong occurred")
}
