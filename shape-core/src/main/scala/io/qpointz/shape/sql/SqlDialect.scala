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

case class SqlDialect(driver:String, calciteDialect:org.apache.calcite.sql.SqlDialect)

object SqlDialects {
  val H2Dialece: SqlDialect = SqlDialect("org.h2.Driver", org.apache.calcite.sql.dialect.H2SqlDialect.DEFAULT)
}
