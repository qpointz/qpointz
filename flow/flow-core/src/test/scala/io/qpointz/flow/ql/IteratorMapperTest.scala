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

package io.qpointz.flow.ql

import io.qpointz.flow.ql.types.QAny
import io.qpointz.flow.{Record, RecordReader}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class IteratorMapperTest extends AnyFlatSpec with Matchers {
  import io.qpointz.flow.ql.types.QAny._
  val recs = Seq(
    Record("id"->1, "name"->"John", "lastname"->"Doe"),
    Record("id"->2, "name"->"Sarah", "lastname"->"Doe"),
    Record("id"->3, "name"->"Kid", "lastname"->"Jay")
  )

  def reader: RecordReader = RecordReader.fromIterable(recs)

  behavior of "FunctionCall"

  it should "execute" in {
    val q = QlQuery(ProjectionExpression(Seq(
      AliasExpressionElement(
        FunctionCallExpression(
          l => { QAny(l(0).asStringOr("missing") + l(1).asStringOr("missing"))}
          , List(
            RecordAttribute("name"),
            RecordAttribute("lastname")
          )
        )
        , "aplusb"))))
    val res = IteratorMapper(q)(reader.iterator).toSeq
    println(res)
  }

}
