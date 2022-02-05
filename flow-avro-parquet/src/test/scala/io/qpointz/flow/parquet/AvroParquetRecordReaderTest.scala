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

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.hadoop.util.HadoopInputFile
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID

class AvroParquetRecordReaderTest extends AnyFlatSpec with Matchers {

  behavior of "read"


  def newPath:String = {
    val p = s"./.test/parquet-writer/readFile${UUID.randomUUID().toString()}.parquet"
    ParquetUtils.writeTestFile(p)
    p
  }

  private def readTest() = {
    val readPath = newPath
    val s  = new AvroParquetRecordReaderSettings()
    val cfg = new Configuration()
    s.inputFile = HadoopInputFile.fromPath(new Path(readPath), cfg)
    new AvroParquetRecordReader(s)
  }

  it should "read records" in {
    readTest().toSeq should not be empty
  }

  it should "return all attributes" in {
    val r = readTest().head
    r.attributes.keys should contain allOf("a","b","c")
  }

}
