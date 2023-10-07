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
import org.apache.calcite.sql.parser.SqlParserPos
import org.apache.calcite.sql.{SqlIdentifier, SqlNodeList, SqlSelect}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters._

class SqlSessionTest extends AnyFlatSpec with Matchers with SqlBaseTest {

  import io.qpointz.shape.sql._

  behavior of "execQuery"

  it should "execute string" in {
    val a = SqlSession.execQuery(session, "SELECT * FROM depts WHERE id=5").asArraySeq
    a.length shouldBe(1)
  }

  it should "execute string with params" in {
    val id=10
    val a = SqlSession.execQuery(session, "SELECT * FROM depts WHERE id=?", Array(id)).asCompactMap
    a.data.length shouldBe (1)
  }

  it should "execute sqlselect" in {
    val select = SqlNodeList.of(new SqlIdentifier(List("ID").asJava, SqlParserPos.ZERO))
    val from = new SqlIdentifier(List("DEPTS").asJava,SqlParserPos.ZERO)
    val sq = new SqlSelect(SqlParserPos.ZERO, null, select, from, null, null,null,null,null,null,null,null)
    val a = SqlSession.execQuery(session, sq).asCompactMap
    a.data.length>0 shouldBe (true)
  }

}
