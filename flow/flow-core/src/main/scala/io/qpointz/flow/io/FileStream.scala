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

package io.qpointz.flow.io

import io.qpointz.flow.{EntryDefinition, Metadata}
import org.json4s.{CustomSerializer, JObject}
import io.qpointz.flow.MetadataMethods._
import java.io.{File, FileInputStream, InputStream}


class FileStreamSource(val file:File) extends InputStreamSource {
  override lazy val inputStream: InputStream = new FileInputStream(file)
  override val metadataGroupKey: String = "qp:stream-source/file"
  override val metadata: Metadata = Seq(
    (EntryDefinition[String](metadataGroupKey, "path") ,file.getAbsolutePath)
  )
}

object FileStreamSource {
  import io.qpointz.flow.serialization.Json._
  import org.json4s.JsonDSL._

  object Serializer extends CustomSerializer[FileStreamSource](implicit format=>(
    {
      case o:JObject =>
        val path = (o \ "path").extract[String]
        new FileStreamSource(new File(path))
    },
    {
      case fs:FileStreamSource => hint ~ ("path" -> fs.file.getAbsolutePath)
    }
  ))
}