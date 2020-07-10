package org.scalax.kirito.poi.writer

import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneId, ZoneOffset}
import java.util.Date

import net.scalax.cpoi.rw.{CellWritersImplicits, ImmutableCellReadersImplicits}
import net.scalax.cpoi.api._
import cats.syntax.all._

trait CustomWriters extends CellWritersImplicits with ImmutableCellReadersImplicits {
  self =>

  implicit val longPoiWriter: CellWriter[Long]             = CellWriter[Double].contramap(_.toDouble)
  implicit val intPoiWriter: CellWriter[Int]               = CellWriter[Double].contramap(_.toDouble)
  implicit val bigDecimalPoiWriter: CellWriter[BigDecimal] = CellWriter[Double].contramap(_.toDouble)
  implicit val localDatePoiWriter: CellWriter[LocalDate] = CellWriter[Date].contramap { (s) =>
    val zoneId = ZoneId.systemDefault()
    val zdt    = s.atStartOfDay(zoneId)
    Date.from(zdt.toInstant())
  }
  implicit val localDateTimePoiWriter: CellWriter[LocalDateTime] = CellWriter[Date].contramap { (s) =>
    val zoneId = ZoneId.systemDefault()
    val zdt    = s.atZone(zoneId)
    Date.from(zdt.toInstant())
  }

  implicit val longPoiReader: CellReader[Long]             = CellReader[Double].map(_.toLong)
  implicit val intPoiReader: CellReader[Int]               = CellReader[Double].map(_.toInt)
  implicit val bigDecimalPoiReader: CellReader[BigDecimal] = CellReader[Double].map(BigDecimal.apply)

  class EmptyCell
  object EmptyCell {
    val value                                              = new EmptyCell
    implicit val emptyCellPoiWriter: CellWriter[EmptyCell] = CellWriter[Option[String]].contramap(s => Option.empty)
  }

}

object CustomWriters extends CustomWriters
