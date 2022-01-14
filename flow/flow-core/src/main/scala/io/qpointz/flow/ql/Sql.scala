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

package io.qpointz.flow.ql

import io.qpointz.flow.ql.types._
import org.apache.calcite.avatica.util.Quoting
import org.apache.calcite.sql.`type`.SqlTypeName
import org.apache.calcite.sql.{SqlBasicCall, SqlFunction, SqlIdentifier, SqlLiteral, SqlNode, SqlSelect}
import org.apache.calcite.sql.parser.SqlParser

import scala.jdk.CollectionConverters._

object Sql {

  def parse(sql:String)(k:SqlParser=>SqlNode):SqlNode = {
    val parserBuilder = SqlParser
      .config()
      .withQuoting(Quoting.BACK_TICK)
    val sqlParser = SqlParser.create(sql, parserBuilder)
    k(sqlParser)
  }

  def parseStatement(stmt:String):SqlNode = parse(stmt)(_.parseStmt())
  def parseExpression(exp:String):SqlNode = parse(exp)(_.parseExpression())

  def asExpression(n:SqlNode):QlExpression = {
    def identifier(i:SqlIdentifier) = i.names.asScala.toList match {
      case a :: Nil => RecordAttribute(a)
      case g :: k :: Nil if (g.length>1 && g.startsWith(":")) => MetadataEntry(g.stripPrefix(":"),k)
      case _ => throw new RuntimeException(s"Wrong identifier ${i.getSimple}")
    }

    def literal(v:SqlLiteral) : Constant = (v.getTypeName) match {
        case SqlTypeName.INTEGER => Constant(QInt(v.intValue(true)))
        case SqlTypeName.BIGINT => Constant(QLong(v.longValue(true)))
        case SqlTypeName.DECIMAL => Constant(QDouble(v.bigDecimalValue().doubleValue()))
        case x => throw new RuntimeException(s"${x} literals not supported")
    }

    n match {
      case i: SqlIdentifier => identifier(i)
      case f: SqlBasicCall => FunctionCallExpression(f.getOperator.getName, f.getOperandList.asScala.map(asValueExpression).toList)
      case f: SqlLiteral => literal(f)
      case n => throw new RuntimeException(s"${n.getKind.lowerName} not implemented yet")
    }
  }

  def asValueExpression(n:SqlNode):QlValueExpression = asExpression(n) match {
    case ve:QlValueExpression => ve
    case _ => throw new RuntimeException(s"${n.toString} not value expression")
  }

  /*
  def selectToQuery(select: SqlSelect): QlQuery = {
    val proj = select.getSelectList.getList.asScala
        .map(asValueExpression)
        .toList
  }*/
/*
  def apply(sql: String):QlQuery = {
      parseStatement(sql) match {
        case select:SqlSelect => selectToQuery(select)
        case _ => throw new RuntimeException("Only SELECT statement supported")
      }
  }
*/
}
