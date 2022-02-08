/*
 * Copyright 2022 qpointz.io
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

package io.qpointz.flow.receipts.impl

import io.qpointz.flow.receipts.Receipt
import io.qpointz.flow.serialization.Json._
import io.qpointz.flow.{RecordReader, RecordWriter}
import org.json4s.Extraction._
import org.json4s.JsonDSL._
import org.json4s._


class Convert(val reader:RecordReader, val writer:RecordWriter) extends Receipt {
  override def run(): Unit = {
    writer.open()
    reader.foreach(writer.write)
    writer.close()
  }
}

class ConvertSerializer extends CustomSerializer[Convert] (implicit fmt => (
  {case jo:JObject =>
    val writer = (jo \ "writer").extract[RecordWriter]
    val reader = (jo \ "reader").extract[RecordReader]
    new Convert(reader, writer)
  },
  {
    case co:Convert =>
      hint[Convert] ~ ("reader" -> decompose(co.reader)) ~ ("writer" -> decompose(co.writer))
  }
))
