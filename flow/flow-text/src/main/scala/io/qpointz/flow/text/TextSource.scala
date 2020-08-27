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

import java.io._

import io.qpointz.flow.{Metadata, MetadataGroupOwner, MetadataProvider}

trait TextSource extends MetadataProvider with MetadataGroupOwner {
    def asReader():Reader
}

import io.qpointz.flow.MetadataMethods._

object TextSource {

    def apply(content:String):TextSource = new StringTextSource(content)

    def apply(file:File):TextSource = new FileTextSource(file)

    def apply(stream:InputStream):TextSource = new StreamTextSource(stream)
}

class StreamTextSource(val stream:InputStream)  extends TextSource {

    lazy val r = new InputStreamReader(stream)

    override def asReader(): Reader = {
        r
    }

    override lazy val metadata: Metadata = List()
    override val metadataGroupKey: String = "formats:text:source:stream"
}

class FileTextSource(val file:File) extends StreamTextSource(new FileInputStream(file)){
    override val metadataGroupKey: String = "formats:text:source:file"
    override lazy val metadata: Metadata = Seq(
        (metadataGroupKey,"path",file.getAbsolutePath)
    )
}

class StringTextSource(val content:String) extends TextSource {
    override val metadataGroupKey: String = "formats:text:source:text"
    override def asReader(): Reader = new StringReader(content)
    override lazy val metadata: Metadata = Seq(
        (metadataGroupKey,"inline",true)
    )
}
