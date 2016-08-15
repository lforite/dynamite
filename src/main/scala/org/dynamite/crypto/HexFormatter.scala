package org.dynamite.crypto

private[dynamite] object HexFormatter {
  def toHexFormat(bytes: Array[Byte]) = bytes.map("%02x" format _).mkString
}
