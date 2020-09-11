/*
 * Copyright 2020 qpointz.io
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

package io.qpointz.flow.parquet

import java.nio.file.{Files, Paths}

import io.qpointz.flow.Record
import org.apache.avro.SchemaBuilder
import org.apache.hadoop.fs.Path

object ParquetUtils {

  def writeTestFile(fp:String): Unit = {
    val filePath = Paths.get(fp)
    if (Files.exists(filePath)) {
      Files.delete(filePath)
    }

    val as = new ConstantAvroScemaSource(SchemaBuilder
      .record("default")
      .fields()
      .requiredString("a")
      .requiredString("b")
      .requiredString("c")

      .endRecord()
    )
    val s = new AvroParquetRecordWriterSettings()
    s.path = new Path(filePath.toAbsolutePath.toString)
    s.schema = as

    val w = new AvroParquetRecordWriter(s)
    w.open()

    for (i<- 1 to 1000) {
      w.write(Record("a" -> "a1", "b" -> "b1", "c" -> "c1"))
    }

    w.close()

  }


}
