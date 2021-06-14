/*
 * Copyright 2021 qpointz.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.qpointz.flow.avro

import io.qpointz.flow.Record
import org.apache.avro.SchemaBuilder
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.Paths

class AvroRecordWriterTest extends AnyFlatSpec with Matchers {

  behavior of "write"

  it should "write simple" in {
    val as = new ConstantAvroScemaSource(SchemaBuilder
      .record("default")
      .fields()
      .requiredString("a")
      .requiredString("b")
      .endRecord()
    )

    val st = new AvroRecordWriterSettings()
    st.schema = as
    st.path = Paths.get("./target/test-out/writeavro.avro")
    val w = new AvroRecordWriter(st)
    w.open()
    w.write(Record("a" -> "a1", "b" -> "b1"))
    w.write(Record("a" -> "a2", "b" -> "b2"))
    w.write(Record("a" -> "a3", "b" -> "b3"))
    w.close()
  }

}
