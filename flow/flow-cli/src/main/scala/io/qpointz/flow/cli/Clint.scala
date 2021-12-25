/*
 * Copyright 2021 qpointz.io
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

package io.qpointz.flow.cli

import org.apache.calcite.avatica.util.Quoting
import org.apache.calcite.sql.SqlNode
import org.apache.calcite.sql.parser.SqlParser
import org.apache.calcite.sql.validate.SqlConformance

object Clint {

  def parse(sql:String)(k:SqlParser=>SqlNode):SqlNode = {
    val parserBuilder = SqlParser
      .config()
      .withQuoting(Quoting.BACK_TICK)
      .withConformance(SqlConformance.PRAGMATIC_2003)
    val sqlParser = SqlParser.create(sql, parserBuilder)
    k(sqlParser)
  }

  def parseStatement(stmt:String):SqlNode = parse(stmt)(_.parseStmt())

  def parseExpression(exp:String):SqlNode = parse(exp)(_.parseExpression())

  def main(args:Array[String]):Unit = {
    val node = parseStatement("select * from `record` where a>0")
    println(node)
  }
}
