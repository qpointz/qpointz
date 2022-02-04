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

package io.qpointz.flow

import io.qpointz.flow.serialization.JsonProtocol
import org.json4s.{CustomSerializer, Extraction}
import org.json4s.JsonAST.JObject
import io.qpointz.flow.serialization.Json._
import org.json4s.JsonDSL._


trait RecordReaderAlike

trait RecordReader extends Iterable[Record] with WithOperationContext {

}

object RecordReaderSerializer extends CustomSerializer[RecordReader](implicit format =>(
  {case jo: JObject => jo.extract[Any].asInstanceOf[RecordReader]},
  PartialFunction.empty)) //json4s collection serialization tweak

object RecordReader {

  val jsonProtocol = JsonProtocol(RecordReaderSerializer)

  def fromIterable(iter:Iterable[Record]):RecordReader = new RecordReader {
    override def iterator: Iterator[Record] = iter.iterator
  }

}

class InMemoryReader(val records:List[Record]) extends RecordReader {
  override def iterator: Iterator[Record] = records.iterator
}

object InMemoryReaderSerializer extends CustomSerializer[InMemoryReader] (implicit format => (
  {case jo:JObject =>
    val recs = (jo \ "records").extract[List[Record]]
    new InMemoryReader(recs)
  },
  {case i:InMemoryReader =>
    hint[InMemoryReader] ~ ("records" -> Extraction.decompose(i.records) )
  }
)) {
  val jsonProtocol = JsonProtocol(flowQuids.reader("inmemory"), InMemoryReaderSerializer)
}
