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

import io.qpointz.flow.serialization.JsonProtocol
import io.qpointz.flow.{Metadata, MetadataAwareWithId, QIds, QTypeId, TypeId}
import org.json4s.{CustomSerializer, JObject}

import java.io.{File, FileInputStream, InputStream}


class FileStreamSource(val file:File) extends InputStreamSource with MetadataAwareWithId {
  override lazy val inputStream: InputStream = new FileInputStream(file)
  override val metaId = FileStreamSource.typeId
  override val metadata: Metadata = Seq(
    meta("path",file.getAbsolutePath)
  )
}

object FileStreamSource {
  import io.qpointz.flow.serialization.Json._
  import org.json4s.JsonDSL._

  val typeId = QIds.Stream.inputStreamId.typeId("file")
  val jsonProtocol = JsonProtocol(typeId, Serializer)

  def apply(file:File):FileStreamSource = new FileStreamSource(file)

  object Serializer extends CustomSerializer[FileStreamSource](implicit format=>(
    {case o:JObject =>
        val path = (o \ "path").extract[String]
        new FileStreamSource(new File(path))},
    {case fs:FileStreamSource => hint[FileStreamSource] ~ ("path" -> fs.file.getAbsolutePath)}
  ))}