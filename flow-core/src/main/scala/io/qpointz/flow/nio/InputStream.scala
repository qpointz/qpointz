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

package io.qpointz.flow.nio

import io.qpointz.flow.{Metadata, MetadataAwareWithId, flowQuids}

import java.io.{InputStream, InputStreamReader, Reader}

object InputStreamSource {
  implicit class InputStreamMethods(stream: InputStreamSource) {
    def reader : Reader = new InputStreamReader(stream.inputStream)
  }
  def apply(stream:InputStream):InputStreamStreamSource = new InputStreamStreamSource(stream)
}

trait InputStreamSource extends StreamSource {
  def inputStream: InputStream
}

class InputStreamStreamSource(stream:InputStream) extends InputStreamSource with MetadataAwareWithId {
  val metaId = flowQuids.inputStream("input-stream")
  override def inputStream: InputStream = stream
  override val metadata: Metadata = Seq()
}


