/*
 *
 *  Copyright 2022 qpointz.io
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.qpointz.flow.sql

import io.qpointz.flow.RecordReader
import org.apache.calcite.rel.logical.LogicalProject
import org.apache.calcite.rex.RexBuilder
import org.apache.calcite.schema.SchemaPlus
import org.apache.calcite.sql.{SqlBinaryOperator, SqlOperator}
import org.apache.calcite.sql.`type`.SqlTypeName
import org.apache.calcite.sql.fun.SqlStdOperatorTable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RelToRecordTest extends AnyFlatSpec with Matchers with FlowSqlSpec {

  override def testSchema: SchemaPlus = calciteSchema("test", Seq(
    table("CLIENT",
      attribute("ID", SqlTypeName.VARCHAR),
      attribute("FIRST_NAME", SqlTypeName.VARCHAR, false),
      attribute("LAST_NAME", SqlTypeName.VARCHAR, false),
      attribute("EMAIL", SqlTypeName.VARCHAR, true),
      attribute("GENDER", SqlTypeName.VARCHAR, true),
      attribute("BIRTHDAY", SqlTypeName.VARCHAR, true),
      attribute("COUNTRY_CODE", SqlTypeName.VARCHAR, nullable = false)
    ),
    table("COUNTRY",
      attribute("ID", SqlTypeName.VARCHAR),
      attribute("CODE", SqlTypeName.VARCHAR),
      attribute("NAME", SqlTypeName.VARCHAR)
    )))

  behavior of "TableScan translation"

  it should "translate" in  {
    val reader = readerFromResource("reldata/CLIENT.csv")
    val src = Map( List("test", "CLIENT")->reader )
    val rel = relBuilder().scan("test","CLIENT").build();
    val records = RelToRecord(rel, src).toList
    records.size > 0 shouldBe true
  }

  behavior of "LogicalProject translation"

  def reader: RecordReader = readerFromResource("reldata/CLIENT.csv")
  lazy val sources = Map(List("test", "CLIENT") -> reader)

  it should "project attributes" in {
    val builder = relBuilder()

    val rel = builder
      .scan("test","CLIENT")
      .project(
        builder.alias(builder.call(SqlStdOperatorTable.PLUS,
                                    builder.literal("HELLO"),
                                    builder.literal("10")),
                      "EXP1")
      )
      .build()
    val proj = rel.asInstanceOf[LogicalProject]
    val exp = proj.getProjects.get(0)
    println(exp)
    //val r = RelToRecord(rel, sources).take(1)
    //r.toList.size shouldBe 2
  }

}
