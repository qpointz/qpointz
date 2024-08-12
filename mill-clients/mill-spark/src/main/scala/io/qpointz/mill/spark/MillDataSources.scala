package io.qpointz.mill.spark

import io.qpointz.mill.proto.common.Table
import org.apache.hadoop.conf.Configuration
import org.apache.parquet.hadoop.api.ReadSupport
import org.apache.parquet.io.api.RecordMaterializer
import org.apache.parquet.schema.MessageType
import org.apache.spark.sql.connector.catalog.{Table, TableCapability, TableProvider}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util
import org.apache.spark.sql._
import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, DataSourceRegister, RelationProvider}
import org.apache.spark.sql.types._

class DefaultSource extends TableProvider
    with DataSourceRegister
    with CreatableRelationProvider
 {

  override def inferSchema(options: CaseInsensitiveStringMap): StructType = {
    StructType(
      StructField("a", StringType, true) :: Nil
    )
  }

  override def getTable(schema: StructType, partitioning: Array[Transform], properties: util.Map[String, String]): Table = {
    MillTable()
  }

  class MillTable extends Table {

    override def name(): String =


    override def schema(): StructType = ???

    override def capabilities(): util.Set[TableCapability] = ???
  }


   override def shortName(): String = "mill"

   override def createRelation(sqlContext: SQLContext, mode: SaveMode, parameters: Map[String, String], data: DataFrame): BaseRelation = {
     ???
   }
 }
