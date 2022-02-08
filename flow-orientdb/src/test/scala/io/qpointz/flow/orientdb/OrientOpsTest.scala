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
 *  limitations under the License
 */

package io.qpointz.flow.orientdb

import com.orientechnologies.orient.core.db.{ODatabaseSession, OrientDB}
import com.orientechnologies.orient.core.record.impl.ODocument
import io.qpointz.flow.orientdb.test.UsingMemDb
import org.json4s.{DefaultFormats, Formats, NoTypeHints}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

case class TestClass(a:String, b:String, c:Int)

class OrientOpsTest extends AnyFlatSpec with Matchers with UsingMemDb {

  behavior of "OResultSetImplicits"

  import OrientOps._

  def testdb(fixture: (OrientDB, ODatabaseSession) => Unit) = withSession(Some("sql/testdb.sql"))(fixture)

  it should "return results" in testdb { (_, k) =>
    k.query("SELECT FROM TestClass")
      .asIterator
      .length shouldBe 7
  }

  it should "return documents" in testdb { (_, k) =>
    k.query("SELECT FROM TestClass")
      .asRecords[ODocument]
      .filter(_.isDefined)
      .map(_.get)
      .length shouldBe 7
  }

  it should "return jvalue" in testdb { (_, s) =>
    s.query("SELECT FROM TestClass")
      .asJson
      .length shouldBe 7
  }

  it should "return insts" in testdb { (_, s) =>
    implicit val fmt:Formats = DefaultFormats + NoTypeHints
    s.query("SELECT FROM TestClass")
      .as[TestClass]
      .length shouldBe 7
  }
}

