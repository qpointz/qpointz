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

package io.qpointz.flow.cli.commands

import io.qpointz.flow.catalogue.LocalCatalogue
import io.qpointz.flow.cli.CliSubcommand
import io.qpointz.flow.serialization.Json.formats
import org.json4s.Extraction
import org.json4s.jackson.JsonMethods.pretty
import picocli.CommandLine.{Command, Parameters}

import java.nio.file.Paths

@Command(name="sql")
class SqlCommand extends CliSubcommand {

  implicit val fmt = formats

  @Parameters(index = "0", arity = "1")
  var sql :String = _

  override def run(): Unit = {
    val gc = new LocalCatalogue(Paths.get(".flow"))
    gc.runSql(sql).get.foreach(x=> terminal.writer.println(pretty(Extraction.decompose(x))))
  }
}
