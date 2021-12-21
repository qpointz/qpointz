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

import io.qpointz.flow.{EntryDefinition, Metadata}
import io.qpointz.flow.serialization.Json
import org.json4s.{CustomSerializer, JObject}
import org.json4s.JsonDSL._

import java.io.{File, FileInputStream, InputStream}

class FileTextSource(val inputFile:File) extends TextSourceStream {
  import io.qpointz.flow.MetadataMethods._

  override def stream(): InputStream = {
    new FileInputStream(inputFile)
  }

  override val metadataGroupKey: String = "formats:text:source:file"
  override lazy val metadata: Metadata = Seq(
    (EntryDefinition[String](metadataGroupKey, "path") ,inputFile.getAbsolutePath)
  )
}

class FileTextSourceSerializer extends CustomSerializer[FileTextSource] (format=>{
  implicit val fmt = format
  (
    {case jo : JObject =>
      val fpath = (jo \ "file").extract[String]
      new FileTextSource(new File(fpath))
    },
    {case fs : FileTextSource => Json.hint ~ ("file" -> fs.inputFile.getAbsolutePath)}
  )})
