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

package io.qpointz.flow.transform.simple.integration
import java.io.File
import java.nio.file.{Files, Path, Paths}

import io.qpointz.flow.parquet.{AvroParquetRecordWriter, AvroParquetRecordWriterSettings, ConstantAvroScemaSource}
import io.qpointz.flow.text.{CsvRecordReader, CsvRecordReaderSettings, TextSource}
import io.qpointz.flow.transform.simple.{WriteRecords, WriteRecordsSettings}
import org.apache.avro.SchemaBuilder
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers

class WriteRecordsTestInt extends org.scalatest.flatspec.AnyFlatSpec with Matchers with BeforeAndAfterAll{


  override def beforeAll(): Unit = {
    Files.deleteIfExists(Paths.get("./tmp/write-records-it-test.parquet"))
    Files.deleteIfExists(Paths.get("./tmp/write-records-it-test.parquet.crc"))
  }

  it should "pass writing" in {

    val csvSettings = new CsvRecordReaderSettings()
    csvSettings.lineSeparator = Array('\n')
    csvSettings.delimiter = ","
    csvSettings.headerExtractionEnabled = true
    val source: TextSource = TextSource(new File(s"flow/test/formats/csv/good.csv"))
    val csvReader = new CsvRecordReader(source, csvSettings)


    val pqSchema = new ConstantAvroScemaSource(SchemaBuilder
      .record("default")
      .fields()
        .requiredString("id")
        .requiredString("first_name")
        .requiredString("last_name")
        .requiredString("email")
        .requiredString("gender")
        .requiredString("ip_address")
        .requiredString("date1")
        .requiredString("date2")
        .requiredString("date3")
        .requiredString("inscope")
        .requiredString("lon")
        .requiredString("lat")
      .endRecord()
    )
    val pqSettings = new AvroParquetRecordWriterSettings()
    pqSettings.schema = pqSchema
    pqSettings.path("./tmp/write-records-it-test.parquet")

    val pqWriter = new AvroParquetRecordWriter(pqSettings)

    val wrSettings = new WriteRecordsSettings()
    wrSettings.openWriter(true)
    wrSettings.closeWriter(true)

    val wr = new WriteRecords(csvReader, pqWriter, wrSettings)

    wr.transform()

  }
}
