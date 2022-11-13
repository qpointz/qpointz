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

import org.apache.calcite.rel.externalize.RelWriterImpl
import org.apache.calcite.schema.SchemaPlus
import org.apache.calcite.sql.`type`.SqlTypeName
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters._
import java.io.PrintWriter

class FlowSchemaTest extends AnyFlatSpec with Matchers with FlowSqlSpec {

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

  behavior of "AnyThing"

  it should "relbuilder" in {
    val p = planner
    val parsed = p.parse("SELECT CL.* FROM `test`.`CLIENT` CL WHERE CL.`ID`='2'")
    val valid = p.validate(parsed)
    val rel = p.rel(valid).rel

    val pw = new PrintWriter(System.out)
    val w = new RelWriterImpl(pw)
    w.explain(rel, List().asJava)
  }

  it should "create record reader" in {
    val r = readerFromResource("reldata/CLIENT.csv")
    r.toSeq.size > 0 shouldBe (true)
  }

}
