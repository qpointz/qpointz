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
import CsvRecordReaderSettings.asCsvParserSettings
import io.qpointz.flow.text.{TextRecordReader, TextSource}
import io.qpointz.flow.{Metadata, MetadataMethods, OperationContext}

class CsvRecordReader(source:TextSource,
                      settings:CsvRecordReaderSettings = CsvRecordReaderSettings.default)(implicit override val ctx:OperationContext)
  extends TextRecordReader[CsvParser, CsvRecordReaderSettings](source = source, settings = settings) {

  override val metadataGroupKey: String = "formats:text:csv:csvrecordreader"

  override protected def createParser(settings: CsvRecordReaderSettings): CsvParser = {
    new CsvParser(asCsvParserSettings(settings))
  }

  override val metadata: Metadata =  MetadataMethods.empty
}