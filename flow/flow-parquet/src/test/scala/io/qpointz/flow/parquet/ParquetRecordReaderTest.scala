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
import org.apache.parquet.io.InputFile
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ParquetRecordReaderTest extends AnyFlatSpec with Matchers {

  behavior of "read"

  val readPath = "./target/test-out/readFile.parquet"

  ParquetUtils.writeTestFile(readPath)

  it should "read simple file" in  {
    /*val s  = new ParquetRecordReaderSettings()
    val cfg = new Configuration()
    s.inputFile = HadoopInputFile.fromPath(new Path(readPath), cfg)
    val r = new ParquetRecordReader(s)
    val recs = r.toList*/
  }

}
