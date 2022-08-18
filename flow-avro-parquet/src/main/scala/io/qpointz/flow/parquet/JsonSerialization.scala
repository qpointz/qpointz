/*
 *
 *  Copyright 2022 qpointz.io
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.qpointz.flow.parquet

import io.qpointz.flow.serialization.{JsonProtocol, JsonProtocolExtension}
import io.qpointz.flow.{flowQuids, serialization}
import org.apache.hadoop.conf.Configuration

class JsonSerialization extends JsonProtocolExtension {
  override def protocols: Iterable[serialization.JsonProtocol[_]] = List(
    JsonProtocol[AvroParquetRecordWriter](flowQuids.writer("parquet"), new ParquetRecordWriterSerializer())
  )
}
