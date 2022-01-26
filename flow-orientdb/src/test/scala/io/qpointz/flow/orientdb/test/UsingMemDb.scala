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

package io.qpointz.flow.orientdb.test

import com.orientechnologies.orient.core.Orient
import com.orientechnologies.orient.core.config.OGlobalConfiguration
import com.orientechnologies.orient.core.db.{ODatabasePool, ODatabaseSession, ODatabaseType, OrientDB, OrientDBConfig}
import com.orientechnologies.orient.server.OServer
import org.apache.commons.io.IOUtils

import java.nio.charset.StandardCharsets
import java.util.UUID

trait UsingMemDb {

  def createDb(): (OrientDB, ODatabasePool, String, String) = {

    val cfg = OrientDBConfig.builder()
      .addConfig(OGlobalConfiguration.CREATE_DEFAULT_USERS, true)
      .build()

    val orientDb = new OrientDB("memory:.test/testdb", cfg)
    val dbname = s"test${UUID.randomUUID().toString().replace("-", "")}"
    val dbpath = s"memory:.test/${dbname}"
    val username = "admin"
    val password = "admin"

    orientDb.createIfNotExists(dbname, ODatabaseType.MEMORY, cfg)
    val pool = new ODatabasePool(orientDb, dbname, username, password)
    (orientDb, pool, dbname, dbpath)
  }

  def withSession(initsql: Option[String] = None)(fixture: (OrientDB, ODatabaseSession) => Unit): Unit = {
    val (orientDb, pool, dbname, dbpath) = createDb()
    val session = pool.acquire
    if (initsql.isDefined) {
      val rname = initsql.get
      val sql = IOUtils.toString(this.getClass.getClassLoader.getResourceAsStream(rname), StandardCharsets.UTF_8)
      session.execute("SQL", sql, Map())
    }

    try {
      fixture(orientDb, session)
    } finally {
      orientDb.drop(dbname)
      session.close()
      pool.close()
      orientDb.close()
    }
  }

  def withSession(fixture: (OrientDB, ODatabaseSession) => Unit): Unit = {
    withSession(None)(fixture)
  }
}