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

package io.qpointz.flow

import io.qpointz.flow.ql.{FromIdentified, IteratorMapper, SqlStm}

import scala.util.{Failure, Success, Try}

package object catalogue {

  trait Catalogue {

    def source(pck:String, source:String):RecordReader

    def runSql(sql:String):Try[Iterator[Record]] = {
      try {
        val stmt = SqlStm(sql)
        val reader = stmt.from match {
          case None => throw new IllegalArgumentException("No from specified")
          case Some(FromIdentified(pck::src::Nil))=> source(pck, src)
          case Some(FromIdentified(l))=> throw new IllegalArgumentException(s"Unknown identifier ${l}")
          case x => throw new IllegalArgumentException(s"from not support $x")
        }
        Success(IteratorMapper(stmt, true)(reader.iterator))
      } catch {
        case ex:Exception => Failure(ex)
      }
    }
  }

}
