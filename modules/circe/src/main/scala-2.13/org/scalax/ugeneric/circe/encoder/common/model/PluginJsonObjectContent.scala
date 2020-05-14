package org.scalax.ugeneric.circe.encoder.common.model

import asuna.PropertyTag
import io.circe.Encoder
import org.scalax.ugeneric.circe.NameTranslator

abstract class PluginJsonObjectContent[I, Model, Name] {
  def appendField(name: Name, p: Option[NameTranslator]): JsonObjectAppender[Model]
}

object PluginJsonObjectContent {

  implicit final def asunaCirceImplicit[T](implicit t: => Encoder[T]): PluginJsonObjectContent[PropertyTag[T], T, String] = { (name, p) =>
    val nameI = p.map(_.tran(name)).getOrElse(name)
    (data, m) => (nameI, t(data)) :: m
  }

}
