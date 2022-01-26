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

package io.qpointz.flow.orientdb

import com.orientechnologies.orient.core.record.impl.ODocument
import io.qpointz.flow.orientdb.test.UsingMemDb
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PersistenceMethodsTest extends AnyFlatSpec with Matchers with UsingMemDb {

  it should "create" in withSession(Some("sql/testdb.sql")) { (x, k) =>
      assert(true)
    }

  behavior of "create"

  it should "create and load" in withSession { (db, session) =>
      val doc = session.newInstance[ODocument]("test1")
      doc.field("foo", "bar")
      doc.save()
      session.commit()
      val d = session.load[ODocument](doc.getIdentity)
      d.field[String]("foo") shouldBe "bar"
    }
}
