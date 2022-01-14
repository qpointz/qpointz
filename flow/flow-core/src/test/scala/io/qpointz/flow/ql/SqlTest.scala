/*
 * Copyright 2022 qpointz.io
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io.qpointz.flow.ql

import io.qpointz.flow.ql
import io.qpointz.flow.ql.types.{QAny, QDouble}
import org.apache.calcite.sql.{SqlNode, SqlSelect}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters._

class SqlTest extends AnyFlatSpec with Matchers {

  def expList(select:String):List[SqlNode] = {
    Sql.parseStatement(select) match {
      case s:SqlSelect => s.getSelectList.asScala.toList
      case _ => throw new RuntimeException(s" $select Not select statement")
    }
  }


  behavior of "expression parse"

  def exp(exp:String): QlExpression = {
    Sql.asExpression(Sql.parseExpression(exp))
  }

  it should "parse function call" in {
    exp("FOO(`a`, 2)") shouldBe FunctionCallExpression("FOO", Seq(RecordAttribute("a"), Constant(QDouble(2))) )
  }

  it should "parse attributes" in {
    exp("`a`") shouldBe RecordAttribute("a")
  }

  it should "parse metadata calls" in {
    exp("`:g`.t") shouldBe MetadataEntry("g","T")
  }

  it should "parse constants" in {
    exp("1") shouldBe Constant(QDouble(1))
  }
}
