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
 *  limitations under the License
 */

package io.qpointz.flow.text

import io.qpointz.flow.serialization
import io.qpointz.flow.serialization.{JsonFormat, JsonFormatExtension}
import io.qpointz.flow.text.csv.{CsvRecordReaderSettings, CsvRecordWriterSettings}

class Extensions extends JsonFormatExtension {
  override def hintNamespace: String = "qp"

  override def protocols: Iterable[serialization.JsonFormat[_]] = List(
    JsonFormat("input-stream", "text-source-file", Some(new FileTextSourceSerializer())),
    JsonFormat[CsvRecordReaderSettings]("settings", "records-reader-csv"),
    JsonFormat[CsvRecordWriterSettings]("settings", "record-writer-csv"),
  )
}
