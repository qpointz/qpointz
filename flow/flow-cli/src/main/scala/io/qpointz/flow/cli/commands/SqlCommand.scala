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

import de.vandermeer.asciitable.AsciiTable
import io.qpointz.flow.Record
import io.qpointz.flow.catalogue.LocalCatalogue
import io.qpointz.flow.cli.CliSubcommand
import io.qpointz.flow.serialization.Json.formats
import org.fusesource.jansi.Ansi
import org.json4s.Extraction
import org.json4s.jackson.JsonMethods.pretty
import picocli.CommandLine.{Command, Option, Parameters}

import java.nio.file.Paths
import scala.util.{Failure, Success}

@Command(name="sql")
class SqlCommand extends CliSubcommand {

  implicit val fmt = formats

  @Parameters(index = "0", arity = "1")
  var sql :String = _

  @Option(names = Array("--output-format", "-of"), defaultValue = "json" ,arity = "1", required = false)
  var outfmt : String = "json"

  def asJson(recs:Iterator[Record]):Unit = {
    recs.foreach(x=>terminal.writer().println(pretty(Extraction.decompose(x))))
  }

  def asTable(recs:Iterator[Record]): Unit = {
    val ls = recs.take(100).toSeq;
    val cols = ls.map(_.keys).flatten.distinct.toList;


    val at = new AsciiTable()
    at.addRule()
    at.addRow(cols:_*)
    at.addRule()

    val vals = ls
      .map(x=> {cols
        .map(c=> x.getOrElse(c, "<NULL>"))
        .map{
          case null=>"NULL"
          case x => x
        }})

    vals
      .foreach(x=> {
        at.addRow(x:_*)
        //at.addRule()
      })

    at.addRule()

    terminal.writer().println(at.render())
    terminal.flush()
  }

  override def run(): Unit = {
    def out(r:Iterator[Record]) = outfmt match {
      case "json" => asJson(r)
      case "tab" => asTable(r)
    }
    val gc = new LocalCatalogue(Paths.get(".flow"))
    val mayBe = gc.runSql(sql)
    mayBe match {
      case Success(recs) => out(recs)
      case Failure(exception) => throw exception
    }

  }
}
