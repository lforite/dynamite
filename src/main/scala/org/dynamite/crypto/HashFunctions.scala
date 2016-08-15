package org.dynamite.crypto

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.dynamite.dsl._

import scalaz.Scalaz._
import scalaz.\/

private[dynamite] object HashFunctions {
  def hmacSha256(toEncode: String, key: Array[Byte]): HashingError \/ Array[Byte] =
    for {
      algorithm <- "HmacSHA256".right
      dataBytes <- getBytes(toEncode)
      secretKey <- new SecretKeySpec(key, algorithm).right
      mac <- \/.fromTryCatchThrowable[Mac, Throwable](Mac.getInstance(algorithm)) leftMap (t => AlgorithmNotFoundError(algorithm))
      _ <- \/.fromTryCatchThrowable[Unit, Throwable](mac.init(secretKey)) leftMap (t => InvalidSecretKeyError(secretKey))
      result <- \/.fromTryCatchThrowable[Array[Byte], Throwable](mac.doFinal(dataBytes)) leftMap (t => NotInitializedMacError)
    } yield result

  def sha256(toEncode: String): HashingError \/ Array[Byte] =
    for {
      algorithm <- "SHA-256".right
      messageDigest <- \/.fromTryCatchThrowable[MessageDigest, Throwable](MessageDigest.getInstance(algorithm)) leftMap (t => AlgorithmNotFoundError(algorithm))
      toEncodeBytes <- getBytes(toEncode)
      _ <- messageDigest.update(toEncodeBytes).right
      result <- messageDigest.digest().right
    } yield result


  private def getBytes(toConvert: String): EncodingNotFoundError \/ Array[Byte] = {
    \/.fromTryCatchThrowable[Array[Byte], Throwable](toConvert.getBytes("UTF-8")) leftMap (t => EncodingNotFoundError("UTF-8"))
  }
}
