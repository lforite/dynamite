package org.dynamite.crypto

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.dynamite.dsl.SigningError

import scalaz.\/
import scalaz.Scalaz._

trait HashFunctions {
  protected[dynamite] def hmacSha256(data: String, key: Array[Byte]): SigningError \/ Array[Byte] =
    (for {
      algorithm <- "HmacSHA256".right
      mac <- \/.fromTryCatchThrowable[Mac, Throwable](Mac.getInstance(algorithm))
      _ <- \/.fromTryCatchThrowable[Unit, Throwable](mac.init(new SecretKeySpec(key, algorithm)))
      result <- \/.fromTryCatchThrowable[Array[Byte], Throwable](mac.doFinal(data.getBytes("UTF-8")))
    } yield result).leftMap(t => SigningError("Oooooppsssss something wrong occurred"))

  protected[dynamite] def sha256(toEncode: String): SigningError \/ Array[Byte] =
    (for {
      messageDigest <- \/.fromTryCatchThrowable[MessageDigest, Throwable](MessageDigest.getInstance("SHA-256"))
      _ <- \/.fromTryCatchThrowable[Unit, Throwable](messageDigest.update(toEncode.getBytes("UTF-8")))
      result <- \/.fromTryCatchThrowable[Array[Byte], Throwable](messageDigest.digest())
    } yield result) leftMap { t => SigningError("Oooooppsssss something wrong occurred") }
}
