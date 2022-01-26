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

import org.apache.calcite.avatica.util.Quoting
import org.apache.calcite.sql.`type`.SqlTypeName
import org.apache.calcite.sql.parser.SqlParser
import org.apache.calcite.sql._

import scala.jdk.CollectionConverters._

object SqlStm {
  def apply (stm:String):QlQuery = Sql(stm)
}

object SqlExp {
  def apply(exp: String): QlExpression = {
    Sql.expression(Sql.parseExpression(exp))
  }
}

private[ql] object Sql {

  def parse(sql: String)(k: SqlParser => SqlNode): SqlNode = {
    val parserBuilder = SqlParser
      .config()
      .withCaseSensitive(true)
      //.withQuoting(Quoting.BRACKET)
      .withQuoting(Quoting.BACK_TICK)
    val sqlParser = SqlParser.create(sql, parserBuilder)
    k(sqlParser)
  }

  def parseStatement(stmt: String): SqlNode = parse(stmt)(_.parseStmt())

  def parseExpression(exp: String): SqlNode = parse(exp)(_.parseExpression())

  def expression(n: SqlNode): QlExpression = {
    def identifier(i: SqlIdentifier) = i.names.asScala.toList match {
      case a :: Nil => Attribute(a)
      case g :: k :: Nil if (g.length > 1 && g.startsWith(":")) => MetadataEntry(g.stripPrefix(":"), k)
      case _ => throw new RuntimeException(s"Wrong identifier ${i.getSimple}")
    }

    def literal(v: SqlLiteral): Constant = (v.getTypeName) match {
      case SqlTypeName.INTEGER => Constant(v.intValue(true))
      case SqlTypeName.BIGINT => Constant(v.longValue(true))
      case SqlTypeName.DECIMAL => Constant(v.bigDecimalValue().doubleValue())
      case SqlTypeName.CHAR => Constant(v.toValue)
      case x => throw new RuntimeException(s"${x} literals not supported")
    }

    n match {
      case i: SqlIdentifier => identifier(i)
      case f: SqlBasicCall => QlSqlFunction(f);
      case f: SqlLiteral => literal(f)
      case dts: SqlDataTypeSpec => Constant(dts.getTypeName.names.get(0))
      case n => throw new RuntimeException(s"${n.getKind.lowerName} not implemented yet")
    }
  }

  def valueExpression(n: SqlNode): QlValueExpression = expression(n) match {
    case ve: QlValueExpression => ve
    case _ => throw new RuntimeException(s"${n.toString} not value expression")
  }

  def projection(s: SqlSelect): Projection = Projection(s.getSelectList.getList.asScala
    .map(valueExpression)
    .map {
      case a: Attribute => ProjectionElement(a, Some(a.key))
      case FunctionCallDecl1(n, Seq(exp, Attribute(alias))) if n == "AS" => ProjectionElement(exp, Some(alias))
      case e => ProjectionElement(e, None)
    }.toSeq)

  def query(n: SqlNode): QlQuery = {
    n match {
      case s: SqlSelect => QlQuery(projection(s))
      case n => throw new RuntimeException(s"SELECT statement expected but found ${n.toString}")
    }
  }

  def apply(sql: String): QlQuery = {
    parseStatement(sql) match {
      case select: SqlSelect => query(select)
      case _ => throw new RuntimeException("Only SELECT statement supported")
    }
  }

}
