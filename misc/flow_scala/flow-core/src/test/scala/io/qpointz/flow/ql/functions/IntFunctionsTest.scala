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
 *  limitations under the License
 */

package io.qpointz.flow.ql.functions

import io.qpointz.flow.Record
import io.qpointz.flow.ql.{IteratorMapper, SqlStm}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Success

class IntFunctionsTest extends AnyFlatSpec with Matchers with FunctionTests {

  behavior of "CAST"

  it should "cast to int" in {
    sql("SELECT CAST(`in_field` AS INT) AS `a`", Record("in_field" -> "1"))(Record("a"->Success(1)))
  }


}
