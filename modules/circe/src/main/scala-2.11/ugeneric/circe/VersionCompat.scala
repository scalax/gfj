package ugeneric.circe

object VersionCompat {

  type ObjectEncoderType[T] = io.circe.ObjectEncoder[T]
  val ObjectEncoderValue = io.circe.ObjectEncoder

}
