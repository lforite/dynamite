package org.dynamite.ast

import dynamo.ast._
import io.circe.Decoder.Result

private[dynamite] case class ROOT(dynamoType: DynamoType) extends AnyVal

object AwsTypeSerialiser {

  import io.circe._
  import io.circe.syntax._

  implicit val encodeDynamoScalarType: Encoder[DynamoScalarType] = new Encoder[DynamoScalarType] {
    override def apply(a: DynamoScalarType): Json = encodeDynamoType.apply(a)
  }

  implicit val encodeDynamoType: Encoder[DynamoType] = new Encoder[DynamoType] {
    final def apply(a: DynamoType): Json = a match {
      case s: S => encodeS.apply(s)
      case n: N => encodeN.apply(n)
      case m: M => encodeM.apply(m)
      case bool: BOOL => encodeBOOL.apply(bool)
      case l@L(_) => encodeL.apply(l)
      case ns: NS => encodeNS.apply(ns)
      case ss: SS => encodeSS.apply(ss)
      case NULL => encodeNULL.apply(NULL)
    }
  }

  implicit val decodeDynamoType: Decoder[DynamoType] = new Decoder[DynamoType] {
    override def apply(c: HCursor): Result[DynamoType] = c.fields.map(_.headOption) match {
      case Some(Some("S")) => decodeS.tryDecode(c)
      case Some(Some("N")) => decodeN.tryDecode(c)
      case Some(Some("M")) => decodeM.tryDecode(c)
      case Some(Some("BOOL")) => decodeBOOL.tryDecode(c)
      case Some(Some("L")) => decodeL.tryDecode(c)
      case Some(Some("SS")) => decodeSS.tryDecode(c)
      case Some(Some("NS")) => decodeNS.tryDecode(c)
      case Some(Some("NULL")) => decodeNULL.tryDecode(c)
      case Some(Some(_)) => Left(DecodingFailure("", List()))
      case Some(None) => Left(DecodingFailure("", List()))
      case None => Left(DecodingFailure("", List()))
    }
  }

  implicit val encodeROOT: Encoder[ROOT] = new Encoder[ROOT] {
    override def apply(a: ROOT): Json = a.dynamoType match {
      case M(e) => e.toMap.asJson
      case _ => Map[String, String]().asJson
    }
  }

  implicit val decodeRoot: Decoder[ROOT] = new Decoder[ROOT] {
    override def apply(c: HCursor): Result[ROOT] = Decoder[Map[String, DynamoType]].apply(c) match {
      case Right(m) => Right(ROOT(M(m.toList)))
      case Left(e) => Left(e)
    }
  }

  implicit val decodeS: Decoder[S] = Decoder.forProduct1("S")(S.apply)
  implicit val encodeS: Encoder[S] = Encoder.forProduct1("S")(s => s.value)

  implicit val decodeN: Decoder[N] = Decoder.forProduct1("N")(N.apply)
  implicit val encodeN: Encoder[N] = Encoder.forProduct1("N")(n => n.value)

  implicit val decodeM: Decoder[M] = new Decoder[M] {
    override def apply(c: HCursor): Result[M] = c.downField("M").as[Map[String, DynamoType]].right.map(_.toList).right.map(M)
  }

  implicit val encodeM: Encoder[M] = new Encoder[M] {
    override def apply(a: M): Json = Map("M" -> a.elements.toMap).asJson
  }

  implicit val decodeBOOL: Decoder[BOOL] = Decoder.forProduct1("BOOL")(BOOL.apply)
  implicit val encodeBOOL: Encoder[BOOL] = Encoder.forProduct1("BOOL")(b => b.value)

  implicit val decodeL: Decoder[L[_ <: DynamoType]] = Decoder.forProduct1("L")((a: List[_ <: DynamoType]) => L(a))
  implicit val encodeL: Encoder[L[_ <: DynamoType]] = Encoder.forProduct1("L")(l => l.elements)

  implicit val decodeNS: Decoder[NS] = Decoder.forProduct1("NS")(Set[String]).map(_.map(N)).map(NS)
  implicit val encodeNS: Encoder[NS] = Encoder.forProduct1("NS")(ns => ns.numbers.map(_.value))

  implicit val decodeSS: Decoder[SS] = Decoder.forProduct1("SS")(Set[String]).map(_.map(S)).map(SS)
  implicit val encodeSS: Encoder[SS] = Encoder.forProduct1("SS")(ss => ss.strings.map(_.value))

  implicit val decodeNULL: Decoder[NULL.type] = Decoder.forProduct1("NULL")((_: Boolean) => NULL)
  implicit val encodeNULL: Encoder[NULL.type] = Encoder.forProduct1("NULL")((_: NULL.type) => true)

}
