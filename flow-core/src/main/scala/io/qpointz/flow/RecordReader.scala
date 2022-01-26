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

trait RecordReaderAlike

trait RecordReader extends Iterable[Record] with WithOperationContext {

}

object RecordReaderSerializer extends CustomSerializer[RecordReader](implicit format =>(
  {case jo: JObject => jo.extract[Any].asInstanceOf[RecordReader]},
  PartialFunction.empty)) //json4s collection serialization tweak

object RecordReader {

  val jsonProtocol = JsonProtocol(RecordReaderSerializer)

  def fromIterable(iter:Iterable[Record])(implicit ct:OperationContext):RecordReader = new RecordReader {

    override implicit val ctx: OperationContext = ct

    override def iterator: Iterator[Record] = iter.iterator

  }

}
