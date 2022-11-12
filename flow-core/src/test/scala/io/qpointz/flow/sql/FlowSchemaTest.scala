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

import org.apache.calcite.avatica.util.Quoting
import org.apache.calcite.rel.externalize.RelWriterImpl
import org.apache.calcite.schema.{Schema, SchemaPlus}
import org.apache.calcite.sql.`type`.SqlTypeName
import org.apache.calcite.sql.parser.SqlParser
import org.apache.calcite.tools.{FrameworkConfig, Frameworks, RelBuilder}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.{PrintWriter, StringWriter}

class FlowSchemaTest extends AnyFlatSpec with Matchers { {

  import scala.jdk.CollectionConverters._
  import SchemaMethods._

  behavior of "AnyThing"

  val testSchema = schema(Seq(
    table("CLIENT",
      attribute("ID", SqlTypeName.INTEGER),
      attribute("FIRST_NAME", SqlTypeName.VARCHAR, false),
      attribute("LAST_NAME", SqlTypeName.VARCHAR, false),
      attribute("EMAIL", SqlTypeName.VARCHAR,true)
    )))

  def calciteSchema:SchemaPlus = Frameworks
    .createRootSchema(false)
    .add("test", testSchema)


  it should "relbuilder" in {
    val parserConfig = SqlParser.Config.DEFAULT
      .withQuoting(Quoting.BACK_TICK)

    val config = Frameworks.newConfigBuilder()
      .defaultSchema(calciteSchema)
      .parserConfig(parserConfig)
      .build()

    val planner = Frameworks.getPlanner(config)
    val parsed = planner.parse("SELECT A.* FROM `tt`.`aa` A where A.`id`='aaa'")
    val valid = planner.validate(parsed)
    val rel = planner.rel(valid).rel

    val pw = new PrintWriter(System.out)
    val w = new RelWriterImpl(pw)
    w.explain(rel, List().asJava)
  }
}

}
