/*
 * Copyright 2022 qpointz.io
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
 *  limitations under the License.
 */

package io.qpointz.flow.cli

import com.typesafe.scalalogging.Logger
import org.fusesource.jansi.Ansi.ansi
import org.fusesource.jansi.{Ansi, AnsiConsole}
import org.jline.console.CmdLine
import org.jline.console.impl.{Builtins, SystemRegistryImpl}
import org.jline.keymap.KeyMap
import org.jline.reader.impl.DefaultParser
import org.jline.reader._
import org.jline.terminal.TerminalBuilder
import org.jline.widget.TailTipWidgets
import picocli.CommandLine
import picocli.shell.jline3.PicocliCommands
import picocli.shell.jline3.PicocliCommands.PicocliCommandsFactory

import java.nio.file.{Path, Paths}
import java.util.function.Supplier

object CliMain {

  lazy val log = {
    Logger("default")
  }

  def main(args: Array[String]): Unit = {
    def onlyArg(ar: String): Boolean = {
      args.length == 1 && args.map(_.toLowerCase()).find(x => x == ar.toLowerCase()).isDefined
    }

    try {
      System.err.close()
      log.underlying //log tweak
      System.setErr(System.out)
      AnsiConsole.systemInstall()
      if (onlyArg("--repl")) {
        repl(args)
      } else {
        cli(args)
      }
    }
    finally {
      AnsiConsole.systemUninstall()
    }
  }

  def cli(args: Array[String]): Unit = {
    var executionCode = 0 ;
    val terminal = TerminalBuilder
      .builder()
      .jansi(true)
      .build()
    try {
      val command: CliCommand = new CliCommand()
      command.terminal(terminal)
      var exc:Option[Throwable] = None
      val cmd = new CommandLine(command).setExecutionExceptionHandler((ex: Exception, commandLine: CommandLine, parseResult: CommandLine.ParseResult) => {
        exc = Some(ex)
        -1
      })
      executionCode = cmd.execute(args: _*)
      exc match {
        case Some(e) => throw e
        case None =>
      }
    } catch {
      case t: Throwable => {
        terminal.writer().println(ansi().fg(Ansi.Color.RED).toString)
        t.printStackTrace(terminal.writer())
        terminal.writer().println(ansi().reset().toString)
      }
    } finally {
      terminal.flush()
    }
    System.exit(executionCode)
}

  //scalastyle:off
  def repl(args: Array[String]): Unit = {
    val workDir: Supplier[Path] = () => Paths.get(System.getProperty("user.dir"))
    val builtins = new Builtins(workDir, null, null)
    builtins.rename(Builtins.Command.TTOP, "top")
    builtins.alias("zle", "widget")
    builtins.alias("bindkey", "keymap")
    val commands = new CliCommand()
    val factory = new PicocliCommandsFactory()
    var lastEx:Option[Exception] = None
    val cmd = new CommandLine(commands, factory)
      .setExecutionExceptionHandler((ex: Exception, commandLine: CommandLine, parseResult: CommandLine.ParseResult) => {
        lastEx = Some(ex)
        0
      })

    val picoCliCommands = new PicocliCommands(cmd)
    val parser = new DefaultParser()

    try {
      val terminal = TerminalBuilder.builder().build()
      val systemRegistry = new SystemRegistryImpl(parser, terminal, workDir, null)
      systemRegistry.setCommandRegistries(builtins, picoCliCommands)
      val reader = LineReaderBuilder.builder()
        .terminal(terminal)
        .completer(systemRegistry.completer())
        .parser(parser)
        .variable(LineReader.LIST_MAX, 50)
        .build()
      builtins.setLineReader(reader)
      commands.terminal(reader.getTerminal)
      factory.setTerminal(terminal)
      val widgets = new TailTipWidgets(reader, (x: CmdLine) => systemRegistry.commandDescription(x), 3, TailTipWidgets.TipType.COMPLETER)
      widgets.enable()
      val keyMap = reader.getKeyMaps.get("main")
      keyMap.bind(new Reference("tailtip-toggle"), KeyMap.alt("s"))
      val prompt = "fl:> "
      val rightPrompt = null
      var line: String = ""
      while (true) {
        try {
          systemRegistry.cleanUp()
          line = reader.readLine(prompt, rightPrompt, null.asInstanceOf[MaskingCallback], null)
          val r = systemRegistry.execute(line)
          lastEx match {
            case Some(ex) => {
              terminal.writer().println(ansi().fg(Ansi.Color.RED).toString)
              ex.printStackTrace(terminal.writer())
              terminal.writer().println(ansi().reset())
              lastEx = None
            }
            case None =>
          }
        } catch {
          case _: UserInterruptException =>// Ignore
          case _: EndOfFileException => return
          case ex: Exception => systemRegistry.trace(ex)
        }
      }
    } catch {
      case _: Throwable => ???
    }

  }
  //scalastyle:on

}
