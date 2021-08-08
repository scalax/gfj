package org.scalax.ugeneric.circe.decoder

import zsg.macros.single.DefaultValue
import zsg.macros.ByNameImplicit
import zsg.macros.utils.PlaceHolder
import zsg.PropertyTag
import cats.data.Validated
import io.circe._

trait ValidatedDecodeContent[RepTag, Model, T, II, D, Rep] extends Any {
  def getValue(name: II, defaultValue: D, rep: Rep): ValidatedDecoder[T]
}

object ValidatedDecodeContent {

  implicit def asunaCirceDecoder[T](implicit
    dd: ByNameImplicit[Decoder[T]]
  ): ValidatedDecodeContent[PropertyTag[PlaceHolder], PropertyTag[T], T, String, DefaultValue[T], PlaceHolder] =
    new ValidatedDecodeContent[PropertyTag[PlaceHolder], PropertyTag[T], T, String, DefaultValue[T], PlaceHolder] {
      override def getValue(name: String, defaultValue: DefaultValue[T], rep: PlaceHolder): ValidatedDecoder[T] = new ValidatedDecoder[T] {
        override def getValue(json: ACursor): Validated[errorMessage, T] = {
          json.downField(name).focus match {
            case Some(Json.Null) => Validated.invalid(errorMessage.build(name, s"${name}不能为空"))
            case None            => Validated.invalid(errorMessage.build(name, s"${name}不能为空"))
            case Some(j) =>
              j.as(dd.value) match {
                case Left(i) =>
                  Validated.invalid(errorMessage.build(name, i.message))
                case Right(value) => Validated.valid(value)
              }
          }
        }
      }
    }

  /*implicit def asunaOptCirceDecoder[T](
    implicit dd: ByNameImplicit[Decoder[Option[T]]]
  ): Application4[ValidatedDecodeContent, PropertyTag1[PlaceHolder, Option[T]], Option[T], String, DefaultValue[Option[T]], PlaceHolder] =
    new Application4[ValidatedDecodeContent, PropertyTag1[PlaceHolder, Option[T]], Option[T], String, DefaultValue[Option[T]], PlaceHolder] {
      override def application(context: Context4[ValidatedDecodeContent]): ValidatedDecodeContent[Option[T], String, DefaultValue[Option[T]], PlaceHolder] =
        new ValidatedDecodeContent[Option[T], String, DefaultValue[Option[T]], PlaceHolder] {
          override def getValue(name: String, defaultValue: DefaultValue[Option[T]], rep: PlaceHolder): ValidatedDecoder[Option[T]] = new ValidatedDecoder[Option[T]] {
            override def getValue(json: ACursor): Validated[errorMessage, Option[T]] = {
              json.get(name)(dd.value) match {
                case Left(i)      => Validated.invalid(errorMessage.build(name, i.message))
                case Right(value) => Validated.valid(value)
              }
            }
          }
        }
    }*/

  implicit def asunaValidatedCirceDecoder[T](implicit
    dd: ByNameImplicit[ValidatedDecoder[T]]
  ): ValidatedDecodeContent[PropertyTag[PlaceHolder], PropertyTag[T], T, String, DefaultValue[T], PlaceHolder] =
    new ValidatedDecodeContent[PropertyTag[PlaceHolder], PropertyTag[T], T, String, DefaultValue[T], PlaceHolder] {
      override def getValue(name: String, defaultValue: DefaultValue[T], rep: PlaceHolder): ValidatedDecoder[T] = new ValidatedDecoder[T] {
        override def getValue(json: ACursor): Validated[errorMessage, T] = {
          dd.value.getValue(json.downField(name)).leftMap(_.addPrefix(name))
        }
      }
    }

}
