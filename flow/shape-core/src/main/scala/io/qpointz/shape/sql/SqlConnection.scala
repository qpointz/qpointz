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

import org.apache.calcite.sql.SqlSelect

import java.sql.{DriverManager, ResultSet}
import java.util.Properties

case class SqlSession(dialect: SqlDialect, url:String, props:Properties)
object SqlSession {
  def apply(dialect: SqlDialect, url:String):SqlSession = {
    SqlSession(dialect, url, new Properties())
  }
  def apply(dialect: SqlDialect, url: String, user:String, password:String): SqlSession = {
    val p = new Properties()
    p.put("user",user)
    p.put("password",password)
    SqlSession(dialect, url, p)
  }
  def execQuery[T](session:SqlSession, sql:T, array: Array[_])(fsql:(T,SqlSession)=>String):ResultSet = {
    Class.forName(session.dialect.driver)
    val conn = DriverManager.getConnection(session.url, session.props)
    val query = fsql(sql,session)
    val stm = conn.prepareStatement(query)
    array.zipWithIndex.foreach(kv=>stm.setObject(kv._2 + 1,kv._1))
    stm.executeQuery()
  }
  def execQuery(session: SqlSession, sql:SqlSelect, array: Array[_]):ResultSet = execQuery[SqlSelect](session,sql, array){
    (s, session)=>s.toSqlString(session.dialect.calciteDialect).getSql
  }
  def execQuery(session: SqlSession, sql: SqlSelect): ResultSet = {
    execQuery(session, sql, Array())
  }
  def execQuery(session: SqlSession, sql: String, array: Array[_]): ResultSet = execQuery[String](session, sql, array) {
    (s, session: SqlSession) => s
  }
  def execQuery(session: SqlSession, sql: String): ResultSet = {
    execQuery(session, sql, Array())
  }
}
