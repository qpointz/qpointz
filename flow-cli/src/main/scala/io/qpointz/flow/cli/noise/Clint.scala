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
 *  limitations under the License.
 */

package io.qpointz.flow.cli.noise

import org.apache.calcite.avatica.util.Quoting
import org.apache.calcite.sql.SqlNode
import org.apache.calcite.sql.parser.SqlParser

object Clint {

  def parse(sql: String)(k: SqlParser => SqlNode): SqlNode = {
    val parserBuilder = SqlParser
      .config()
      .withQuoting(Quoting.BACK_TICK)
    val sqlParser = SqlParser.create(sql, parserBuilder)
    k(sqlParser)
  }

  def parseStatement(stmt: String): SqlNode = parse(stmt)(_.parseStmt())

  def parseExpression(exp: String): SqlNode = parse(exp)(_.parseExpression())

  def main(args: Array[String]): Unit = {
    /*val node = parseStatement("select CAST(a+2 AS INT), `#g.t`.t = 'sss' as b, c from `record` where 1").asInstanceOf[SqlSelect]
    node.getSelectList
    println(node)*/
    val n = parseStatement("SELECT case " +
      "                             when 1> 0 then SIN(1)" +
      "                             when 3<0 then 12 else 0 END as b")
    println(n)
  }
}
