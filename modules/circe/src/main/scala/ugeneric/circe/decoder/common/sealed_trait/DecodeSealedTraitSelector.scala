package ugeneric.circe.decoder

import zsg.macros.ByNameImplicit
import zsg.macros.single.SealedTag
import io.circe.Decoder
import zsg.Context2

class DecodeSealedTraitSelector[P] {

  trait JsonDecoder[I, II] {
    def getValue(name: II): Decoder[P]
  }

}

object DecodeSealedTraitSelector {

  def apply[T]: DecodeSealedTraitSelector[T]        = value.asInstanceOf[DecodeSealedTraitSelector[T]]
  private val value: DecodeSealedTraitSelector[Any] = new DecodeSealedTraitSelector[Any]

  implicit def asunaCirceSealedDecoder[T, R <: T](
    implicit t: ByNameImplicit[Decoder[R]]
  ): DecodeSealedTraitSelector[T]#JsonDecoder[SealedTag[R], String] = {
    val con = DecodeSealedTraitSelector[T]
    new con.JsonDecoder[SealedTag[R], String] {
      override def getValue(name: String): Decoder[T] = Decoder.instance { _.get(name)(t.value): Decoder.Result[R] }
    }

  }

  implicit def c[H]: Context2[DecodeSealedTraitSelector[H]#JsonDecoder] = DecodeSealedContext[H]

}
