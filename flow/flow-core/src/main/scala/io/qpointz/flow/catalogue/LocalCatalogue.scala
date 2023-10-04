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

package io.qpointz.flow.catalogue

import io.qpointz.flow.RecordReader
import org.apache.commons.io.FileUtils
import org.json4s.jackson.JsonMethods

import java.io.{File, FileInputStream}
import java.nio.file.{Path, Paths}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import io.qpointz.flow.serialization.Json._

class LocalCatalogue(val path:Path) extends Catalogue {

    override def source(pck: String, source: String): RecordReader = {
      val sourceFile:File = Paths.get(path.toString, pck, "sources", s"${source}.json").toFile
      if (!sourceFile.exists()) {
        throw new IllegalArgumentException(s"Source ${pck}.${source} doesn't exists")
      }
      implicit val fmt = formats
      parse(sourceFile).extract[RecordReader]
    }

}
