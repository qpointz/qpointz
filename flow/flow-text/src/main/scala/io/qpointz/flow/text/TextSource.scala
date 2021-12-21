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

import io.qpointz.flow.MetadataMethods._
import io.qpointz.flow.nio.InputStreamSource
import io.qpointz.flow.{Metadata, MetadataGroupOwner, MetadataProvider}

import java.io._

trait TextSource extends MetadataProvider with MetadataGroupOwner {
    def asReader():Reader
}

trait TextSourceStream extends TextSource {
    def stream():InputStream
    lazy val streamReader = new InputStreamReader(this.stream)
    override def asReader(): Reader = streamReader
}

object TextSource {
    def apply(content:String):TextSource = new StringTextSource(content)
    def apply(file:File):TextSource = new FileTextSource(file)
    def apply(stream:InputStream):TextSource = new StreamTextSource(stream)
    def apply(stream:InputStreamSource):TextSource = new StreamTextSource(stream.inputStream)
}

class StreamTextSource(private val inputStream:InputStream)  extends TextSourceStream {
    override def stream(): InputStream = inputStream
    override lazy val metadata: Metadata = List()
    override val metadataGroupKey: String = "formats:text:source:stream"
}


class StringTextSource(val content:String) extends TextSource {
    override val metadataGroupKey: String = "formats:text:source:text"
    override def asReader(): Reader = new StringReader(content)
    override lazy val metadata: Metadata = Seq(
        (metadataGroupKey,"inline",true)
    )
}
