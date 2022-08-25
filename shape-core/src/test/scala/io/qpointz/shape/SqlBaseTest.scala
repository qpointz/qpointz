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

package io.qpointz.shape

import org.h2.tools.RunScript
import org.scalatest.{BeforeAndAfterAll, Suite}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.{FileReader, InputStreamReader}
import java.sql.{Connection, DriverManager, ResultSet}
import java.util.UUID

trait SqlBaseTest extends BeforeAndAfterAll {this: Suite =>

  lazy val dburl = "jdbc:h2:mem:db" + UUID.randomUUID().toString.replace("-","")

  private def reader(resource:String) = {
    val stream = this.getClass.getClassLoader.getResourceAsStream(resource)
    new InputStreamReader(stream)
  }

  lazy val connection = {
    Class.forName("org.h2.Driver")
    val conn = DriverManager.getConnection(dburl)
    conn
  }

  def executeQuery(sql:String):ResultSet ={
    val stmt = connection.prepareStatement(sql)
    stmt.executeQuery()
  }

  override def beforeAll():Unit ={
    RunScript.execute(connection, reader("shape-core/deps.sql"))
  }

  override def afterAll():Unit = {
    connection.close()
  }

}
