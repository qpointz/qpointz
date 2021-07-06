/*
 * Copyright 2020 qpointz.io
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.qpointz.flow.parquet

import java.nio.file.Files

import io.qpointz.flow.Record
import org.apache.avro.{Schema, SchemaBuilder}
import org.apache.hadoop.fs.Path
import java.nio.file.Paths
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AvroParquetRecordWriterTest extends AnyFlatSpec with Matchers {

  behavior of "write"

  it should "write file" in {
    ParquetUtils.writeTestFile("./target/test-out/writeFile.parquet")

  }

}
