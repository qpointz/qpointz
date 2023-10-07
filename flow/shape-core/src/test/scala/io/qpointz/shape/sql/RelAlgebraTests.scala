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

package io.qpointz.shape.sql

import io.qpointz.shape.SqlBaseTest
import org.apache.calcite.adapter.jdbc.{JdbcConvention, JdbcSchema}
import org.apache.calcite.jdbc.{CalciteConnection, Driver}
import org.apache.calcite.rel.rel2sql.RelToSqlConverter
import org.apache.calcite.schema.SchemaFactory
import org.apache.calcite.sql.fun.SqlInternalOperators
import org.apache.calcite.sql.{SqlDialect, SqlOperator}
import org.apache.calcite.sql2rel.SqlToRelConverter
import org.apache.calcite.tools.{Frameworks, RelBuilder}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.sql.DriverManager

class RelAlgebraTests extends AnyFlatSpec with Matchers with SqlBaseTest {

  behavior of "basic"

  it should "build" in {
    //import java.sql.DriverManager
    Class.forName("org.apache.calcite.jdbc.Driver")
    val root = DriverManager.getConnection("jdbc:calcite:").unwrap(classOf[CalciteConnection]).getRootSchema
    val dataSource = JdbcSchema.dataSource(session.url, session.dialect.driver, "","")
    val sp = JdbcSchema.create(root, "H2", dataSource,null,null)
    val np = root.add("H2",sp)
    val config = Frameworks.newConfigBuilder().defaultSchema(np).build()
    val builder = RelBuilder.create(config)
    val relNode = builder
      .scan("DEPTS")
      .project(builder.field("ID"), builder.field("COUNTRY"))
      .build()
    val rs = new RelToSqlConverter(SqlDialect.DatabaseProduct.H2.getDialect)
    println(rs.visitRoot(relNode).asSelect().toSqlString(SqlDialect.DatabaseProduct.H2.getDialect))
  }

  it should "convert to rel" in {
    val root = DriverManager.getConnection("jdbc:calcite:").unwrap(classOf[CalciteConnection]).getRootSchema
    val dataSource = JdbcSchema.dataSource(session.url, session.dialect.driver, "", "")
    val sp = JdbcSchema.create(root, "H2", dataSource, null, null)
    val np = root.add("H2", sp)
    val config = Frameworks.newConfigBuilder().defaultSchema(np).build()
    val planner = Frameworks.getPlanner(config)
    val node = planner.parse("SELECT COUNTRY, GENDER FROM DEPTS WHERE COUNTRY='USA' AND GENDER<>'Male' ORDER BY 1")
    planner.validate(node)
    val rel = planner.rel(node)
    println(rel.rel.explain())
  }

}
