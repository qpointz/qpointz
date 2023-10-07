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

import io.qpointz.flow.Record
import io.qpointz.flow.cli.CliSubcommand
import org.apache.calcite.linq4j.function.Parameter
import org.fusesource.jansi.Ansi.ansi
import org.fusesource.jansi.{Ansi, AnsiConsole}
import picocli.CommandLine.{Command, Option}

@Command(name = "inspect")
class InspectCommand extends CliSubcommand {

  override def run(): Unit = {
    io.qpointz.flow.serialization.Json.jsonProtocols
      .filter(_.typeId.isDefined)
      .map(x => x.typeId.get.toURI)
      .toSeq
      .sorted
      .foreach(terminal.writer().println)
  }
}