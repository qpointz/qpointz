/*
 *
 *  Copyright 2022 qpointz.io
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.qpointz.flow.nio

import io.qpointz.flow.serialization.JsonProtocol
import org.json4s.{CustomSerializer, JObject, JString}

import java.net.URI

object Path {
  def apply(path:String): Path ={
    Path(URI.create(path))
  }

  def apply(path:java.nio.file.Path):Path = {
    Path(path.toString)
  }

  def apply(file: java.io.File):Path = {
    Path(file.toString)
  }
}

object PathSerializer extends CustomSerializer[Path] (implicit format=> (
  {
    case js:JString => Path(js.s)
    case jo:JObject =>
      val uri = (jo \ "uri").extract[String]
      Path(uri)
  },
  {
    case p:Path => JString(p.uri.toString)
  }
)) {
  val jsonProtocol = JsonProtocol(PathSerializer)
}

case class Path(uri:URI) {}