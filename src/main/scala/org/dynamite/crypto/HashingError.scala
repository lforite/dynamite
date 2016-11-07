package org.dynamite.crypto

import javax.crypto.spec.SecretKeySpec

sealed trait HashingError
case class EncodingNotFoundError(encoding: String) extends HashingError
case class AlgorithmNotFoundError(algorithm: String) extends HashingError
case class InvalidSecretKeyError(key: SecretKeySpec) extends HashingError
case object NotInitializedMacError extends HashingError
