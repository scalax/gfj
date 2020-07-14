package org.scalax.ugeneric.slick

import zsg.{ApplicationX6, ApplicationX8}
import zsg.macros.multiply.{ZsgMultiplyGeneric, ZsgMultiplyRepGeneric}
import zsg.macros.single.{ZsgGeneric, ZsgGetterGeneric, ZsgSetterGeneric}
import org.scalax.ugeneric.slick.mutiply.{InsertOrUpdateContext, InsertOrUpdateRep, RepContext, RepNode}
import slick.SlickException
import slick.ast.{MappedScalaType, Node, ProductNode, TypeMapping}
import slick.dbio.DBIO
import slick.jdbc.JdbcProfile
import slick.lifted.{AbstractTable, FlatShapeLevel, ProvenShape, Query, Shape, ShapedValue}
import slick.util.{ConstArray, ProductWrapper, TupleSupport}

import scala.collection.compat._
import scala.reflect.ClassTag

object USlick {

  def mapWithTable[Data, Rep, HListDataType, Table <: AbstractTable[Data], P, RepType, DataType, EncodeRef, Packed1](
    table: Table
  )(
    implicit p: ZsgMultiplyGeneric.Aux[Table, Data, P],
    modelGeneric: ZsgGeneric.Aux[Data, HListDataType],
    app: ApplicationX6[RepNode, RepContext, P, HListDataType, RepType, DataType, EncodeRef, Packed1],
    zsgMultiplyRepGeneric: ZsgMultiplyRepGeneric[Table, Data, RepType],
    zsgGetterGeneric: ZsgGetterGeneric[Data, DataType],
    zsgSetterGeneric: ZsgSetterGeneric[Data, DataType],
    i: ClassTag[Data]
  ): ProvenShape[Data] = {
    val repType = app.application(RepContext.value)
    val shape = new Shape[FlatShapeLevel, Packed1, Data, Packed1] { self =>
      override def pack(value: Packed1): Packed1                              = value
      override def packedShape: Shape[FlatShapeLevel, Packed1, Data, Packed1] = self
      override def buildParams(extract: Any => Data): Packed1                 = repType.buildParams(extract.andThen(zsgGetterGeneric.getter))
      // 这里没有 _1，估计会报错
      override def encodeRef(value: Packed, path: Node): Any = repType.encodeRef(value, path, 1)
      override def toNode(value: Packed): Node = {
        def toBase(v: Any)   = new ProductWrapper(repType.fieldPlus(zsgGetterGeneric.getter(v.asInstanceOf[Data]), List.empty).to(IndexedSeq))
        def toMapped(v: Any) = zsgSetterGeneric.setter(repType.fieldTail(TupleSupport.buildIndexedSeq(v.asInstanceOf[Product]).to(List))._1)
        TypeMapping(ProductNode(ConstArray.from(repType.node(value, List.empty))), MappedScalaType.Mapper(toBase, toMapped, None), i)
      }
    }
    val rep = zsgMultiplyRepGeneric.rep(table)
    ShapedValue(repType.pack(rep), shape)
  }

  trait InsertOrUpdateProfile[Profile <: JdbcProfile, D] {
    protected val profile: Profile
    def updateQ: profile.UpdateActionExtensionMethods[D]
    def insertQ: profile.InsertActionExtensionMethods[D]
  }

  def dataMapper[Data, Rep, HListDataType, PolyModel, PolyTag, PolyType, Table, C[_], P, RepType, DataType, EncodeRef, Packed1](
    query: Query[Table, Data, C],
    poly: PolyModel
  )(
    implicit p: ZsgMultiplyGeneric.Aux[Table, Data, P],
    polyGeneric: ZsgMultiplyGeneric.Aux[PolyModel, Data, PolyTag],
    modelGeneric: ZsgGeneric.Aux[Data, HListDataType],
    app: ApplicationX8[InsertOrUpdateRep, InsertOrUpdateContext, P, HListDataType, PolyTag, RepType, DataType, PolyType, EncodeRef, Packed1],
    zsgPolyRepGeneric: ZsgMultiplyRepGeneric[PolyModel, Data, PolyType],
    zsgMultiplyRepGeneric: ZsgMultiplyRepGeneric[Table, Data, RepType],
    zsgGetterGeneric: ZsgGetterGeneric[Data, DataType],
    // zsgSetterGeneric: ZsgSetterGeneric[Data, DataType],
    i: ClassTag[Data],
    slickJdbcProfile: JdbcProfile
  ): InsertOrUpdateProfile[slickJdbcProfile.type, Data] = {
    val repType = app.application(InsertOrUpdateContext.value)
    val polyRep = zsgPolyRepGeneric.rep(poly)
    val shape = new Shape[FlatShapeLevel, Packed1, Data, Packed1] { self =>
      override def pack(value: Packed1): Packed1                              = value
      override def packedShape: Shape[FlatShapeLevel, Packed1, Data, Packed1] = self
      override def buildParams(extract: Any => Data): Packed1                 = repType.buildParams(extract.andThen(zsgGetterGeneric.getter))
      // 这里没有 _1，估计会报错
      override def encodeRef(value: Packed, path: Node): Any = repType.encodeRef(value, path, 1, polyRep)._1
      override def toNode(value: Packed): Node = {
        def toBase(v: Any) = new ProductWrapper(repType.fieldPlus(zsgGetterGeneric.getter(v.asInstanceOf[Data]), List.empty, polyRep).to(IndexedSeq))
        def toMapped(v: Any) =
          throw new SlickException(
            "Insert or update function can not use to search"
          ) // zsgSetterGeneric.setter(repType.fieldTail(TupleSupport.buildIndexedSeq(v.asInstanceOf[Product]).to(List))._1)
        TypeMapping(ProductNode(ConstArray.from(repType.node(value, List.empty, polyRep))), MappedScalaType.Mapper(toBase, toMapped, None), i)
      }
    }
    val query1 = query.map(table => repType.pack(zsgMultiplyRepGeneric.rep(table)))(shape)
    //val shapeValue = ShapedValue(repType.pack(rep), shape)
    slickJdbcProfile.api.queryUpdateActionExtensionMethods(query1)
    new InsertOrUpdateProfile[slickJdbcProfile.type, Data] {
      override val profile: slickJdbcProfile.type                      = slickJdbcProfile
      override def updateQ: profile.UpdateActionExtensionMethods[Data] = profile.api.queryUpdateActionExtensionMethods(query1)
      override def insertQ: profile.InsertActionExtensionMethods[Data] = profile.api.queryInsertActionExtensionMethods(query1)
    }
  }

}
