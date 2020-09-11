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

package io.qpointz.flow.text

import com.univocity.parsers.common.AbstractWriter
import com.univocity.parsers.csv.CsvWriter
import io.qpointz.flow.{Record, RecordWriter}

trait TextWriterSettings {}

abstract class TextRecordWriter[TWriter <: AbstractWriter[_], TWriterSettings <: TextWriterSettings]
(
  protected val settings: TWriterSettings
) extends RecordWriter {

  protected def createWriter(settings: TWriterSettings): TWriter

  lazy val writer:TWriter = createWriter(settings)

  override def open(): Unit = {
    ctx.log.info("Opening writer")
  }

  override def write(r: Record): Unit = {
    ctx.log.debug(s"Writing record ${r}")
    writer.processRecord(r.attributes)
  }

  override def close(): Unit = {
    ctx.log.info("Closing writer")
    writer.close()
  }
}
