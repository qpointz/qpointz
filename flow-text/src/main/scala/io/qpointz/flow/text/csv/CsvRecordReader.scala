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

package io.qpointz.flow.text.csv

import com.univocity.parsers.csv.CsvParser
import io.qpointz.flow.nio.{InputStreamSource, StreamSource}
import io.qpointz.flow.serialization.Json._
import io.qpointz.flow.serialization.JsonProtocol
import io.qpointz.flow.text.TextRecordReader
import io.qpointz.flow.text.csv.CsvRecordReaderSettings.asCsvParserSettings
import io.qpointz.flow.{Metadata, MetadataMethods, OperationContext, QIds, QTypeId}
import org.json4s.JsonDSL._
import org.json4s.{CustomSerializer, Extraction, JObject}

object CsvRecordReader {
    val typeId:QTypeId = QIds.Record.Reader.reader.typeId("csv")
    val jsonProtocol = JsonProtocol[CsvRecordReader](typeId, CsvRecordReaderSerializer)
}

object CsvRecordReaderSerializer extends CustomSerializer[CsvRecordReader](implicit format=> {(
    {case jo:JObject =>
        val s = (jo \ "settings").extract[CsvRecordReaderSettings]
        val i = (jo \ "source").extract[InputStreamSource]
        new CsvRecordReader(i, s)},
    {case crr:CsvRecordReader =>
          hint[CsvRecordReader] ~ ("settings" -> Extraction.decompose(crr.settings)) ~ ("source" -> Extraction.decompose(crr.stream))}
  )})

class CsvRecordReader(stream:InputStreamSource,
                      settings:CsvRecordReaderSettings = CsvRecordReaderSettings.default)(implicit override val ctx:OperationContext)
  extends TextRecordReader[CsvParser, CsvRecordReaderSettings](stream, settings = settings) {

  override val metaId: QTypeId = CsvRecordReader.typeId

  override protected def createParser(settings: CsvRecordReaderSettings): CsvParser = {
    new CsvParser(asCsvParserSettings(settings))
  }

  override val metadata: Metadata =  MetadataMethods.empty

}