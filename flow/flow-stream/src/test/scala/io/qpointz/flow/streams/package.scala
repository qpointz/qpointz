/*
 * Copyright 2021 qpointz.io
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
 *  limitations under the License.
 */

package io.qpointz.flow

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.qpointz.flow.nio.InputStreamSource
import io.qpointz.flow.text.csv._


package object streams {

  def ratesSource:Source[Record, NotUsed] = Source.fromIterator(()=>ratesRecords.iterator)

  def ratesRecords:Seq[Record] = ratesRecordsReader.toSeq

  def ratesRecordsReader: RecordReader = {
    val st = new CsvRecordReaderSettings(
      headerExtractionEnabled = Some(true),
      format = Some(CsvFormat(lineSeparator=Some("\n")))
    )
    val stream = this.getClass.getClassLoader.getResourceAsStream("flow/stream/testData/rates.csv")
    val src = InputStreamSource(stream)
    new CsvRecordReader(src, st)
  }

}
