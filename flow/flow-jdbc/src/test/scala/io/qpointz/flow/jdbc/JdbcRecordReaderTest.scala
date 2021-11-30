/*
 * Copyright 2020 qpointz.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.qpointz.flow.jdbc

import java.io.InputStreamReader
import java.sql.DriverManager

import org.h2.tools.RunScript
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JdbcRecordReaderTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  val connectionString = "jdbc:h2:mem:test_db_2"

  override def beforeAll(): Unit = {
    Class.forName("org.h2.Driver")
    val conn = DriverManager.getConnection(connectionString)
    val sr = new InputStreamReader(classOf[JdbcRecordReaderTest]
      .getClassLoader
      .getResourceAsStream("testdb.sql"))
    RunScript.execute(conn, sr);
  }

  behavior of "read"

  it should "simple connection" in {
    val settings = JdbcRecordReaderSettings(
      "org.h2.Driver",
      JdbcConnectionUrl(connectionString),
      "select * from person",
      Seq())

    val reader = new JdbcRecordReader(settings)
    val recs = reader.toList
    assert(recs.nonEmpty)
  }

  it should "connection with properties" in {
    val settings = JdbcRecordReaderSettings(
      "org.h2.Driver",
      JdbcConnectionUrlWithProperties("jdbc:h2:mem:test_db_2", Map("DB_CLOSE_DELAY"-> "-1")),
      "select * from person",
      Seq())

    val reader = new JdbcRecordReader(settings)
    val recs = reader.toList
    assert(recs.nonEmpty)
  }

  ignore should "connect with user name/password" in {
    val settings = JdbcRecordReaderSettings(
      "org.h2.Driver",
      JdbcConnectionUrlWithUsername(connectionString, "testuser", "testpassword"),
      "select * from person",
      Seq())

    val reader = new JdbcRecordReader(settings)
    val recs = reader.toList
    assert(recs.nonEmpty)
  }


  it should "accept parameters" in {
    val settings = JdbcRecordReaderSettings(
      "org.h2.Driver",
      JdbcConnectionUrl(connectionString),
      "select * from person where id=? and first_name=?",
      Seq(10, "Pia"))

    val reader = new JdbcRecordReader(settings)
    val recs = reader.toList
    assert (recs.length == 1)
  }

}
