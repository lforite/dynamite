package org.dynamite.crypto

object HexFormatter {
  protected[dynamite] def toHexFormat(bytes: Array[Byte]) =
    bytes.map("%02x" format _).mkString
}
