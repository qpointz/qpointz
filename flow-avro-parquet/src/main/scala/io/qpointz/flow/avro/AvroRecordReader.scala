/*
 *  Copyright 2021 qpointz.io
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package io.qpointz.flow.avro

import io.qpointz.flow.serialization.Json._
import io.qpointz.flow.{OperationContext, Record, RecordReader}
import org.json4s.JsonDSL._
import org.json4s.{CustomSerializer, JObject}

class AvroRecordReader(implicit val ctx:OperationContext) extends RecordReader {
  override def iterator: Iterator[Record] = ???
}

class AvroRecordReaderSerializer extends CustomSerializer[AvroRecordReader](implicit format => (
  {
    case _:JObject => new AvroRecordReader()
  },
  {case _:AvroRecordReader =>
      hint[AvroRecordReader]
  })
)
