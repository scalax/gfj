package org.scalax.kirito.poi.reader

import cats.Semigroup
import cats.data.Validated
import io.circe._
import ugeneric.circe.{UCirce, VersionCompat}

import scala.collection.compat._

sealed trait RowResult[+T]
case class rowMessageContent(rowMsg: List[RowMessage]) extends RowResult[Nothing] {
  def take20: rowMessageContent = rowMessageContent(rowMsg.take(20))
}
case class rowBody[T](body: T) extends RowResult[T]

object RowResult {
  object EmptyTable
  implicit def circeEncoder[T](implicit bodyEncoder: Encoder[T]): VersionCompat.ObjectEncoderType[RowResult[T]] = UCirce.encodeSealed
  implicit def circeDecoder[T](implicit bodyDecoder: Decoder[T]): Decoder[RowResult[T]]                         = UCirce.decodeSealed
}

object rowMessageContent {
  def build(rowNum: Int, fieldName: String, message: String): rowMessageContent        = rowMessageContent(List(RowMessage(rowNum, fieldName, message)))
  def build(rowNum: Int, fieldName: String, messages: List[String]): rowMessageContent = rowMessageContent(messages.map(r => RowMessage(rowNum, fieldName, r)).to(List))

  implicit val circeEncoder: VersionCompat.ObjectEncoderType[rowMessageContent] = UCirce.encodeCaseClass
  implicit val circeDecoder: Decoder[rowMessageContent]                         = UCirce.decodeCaseClass

  implicit val semigroupTypeClass: Semigroup[rowMessageContent] = new Semigroup[rowMessageContent] {
    override def combine(x: rowMessageContent, y: rowMessageContent): rowMessageContent = rowMessageContent(x.rowMsg ::: y.rowMsg)
  }

  implicit def validateCirceEncoder[T](implicit bodyEncoder: Encoder[T]): Encoder[Validated[rowMessageContent, T]] =
    Encoder[RowResult[T]].contramap { (s) =>
      s match {
        case Validated.Invalid(e)  => e
        case Validated.Valid(body) => rowBody(body)
      }
    }

  implicit def validateCirceDecoder[T](implicit bodyEncoder: Decoder[T]): Decoder[Validated[rowMessageContent, T]] =
    Decoder[RowResult[T]].map { (s) =>
      s match {
        case e: rowMessageContent => Validated.Invalid(e)
        case rowBody(body)        => Validated.Valid(body)
      }
    }

}

object rowBody {
  implicit def circeEncoder[T](implicit bodyEncoder: Encoder[T]): VersionCompat.ObjectEncoderType[rowBody[T]] = UCirce.encodeCaseClass
  implicit def circeDecoder[T](implicit bodyDecoder: Decoder[T]): Decoder[rowBody[T]]                         = UCirce.decodeCaseClass
}
