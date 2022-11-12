/*
 *  Copyright 2022 qpointz.io
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.qpointz.flow.sql

import org.apache.calcite.rel.`type`.{RelDataType, RelDataTypeFactory}
import org.apache.calcite.schema.impl.{AbstractSchema, AbstractTable}
import org.apache.calcite.sql.`type`.SqlTypeName
import org.apache.calcite.{schema => CalciteSchema}

import java.util

case class FlowTestAttribute(name:String, sqlType:SqlTypeName, nullable:Boolean)

case class FlowTestTable(name: String, atts: Seq[FlowTestAttribute])
object SchemaMethods {

  import scala.jdk.CollectionConverters._
  def asTable(atts: Seq[FlowTestAttribute]): CalciteSchema.Table = new AbstractTable {
    override def getRowType(typeFactory: RelDataTypeFactory): RelDataType = {
      val ts = atts
        .map(x=> (x.name , typeFactory.createTypeWithNullability(typeFactory.createSqlType(x.sqlType), x.nullable)))
      typeFactory.createStructType(
        ts.map(_._2).asJava,
        ts.map(_._1).asJava
      )
    }
  }

  def schema(tables: Seq[FlowTestTable]) : AbstractSchema = new AbstractSchema {
    override lazy val  getTableMap: util.Map[String, CalciteSchema.Table] = {
      tables.map(x=> (x.name, asTable(x.atts))).toMap.asJava
    }

  }

  def attribute(name: String, sqlT: SqlTypeName = SqlTypeName.VARCHAR, nullable: Boolean = false): FlowTestAttribute = {
    FlowTestAttribute(name, sqlT, nullable)
  }

  def table(name:String, atts: FlowTestAttribute*):FlowTestTable = {
    FlowTestTable(name, atts)
  }



}
