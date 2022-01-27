/*
 *  Copyright 2022  qpointz.io
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

import io.qpointz.flow.cli.CliCommand
import picocli.CommandLine
import picocli.CommandLine.Command

import java.io.InputStreamReader
import java.nio.CharBuffer

@Command(name="repl")
class ReplCommand extends Runnable {


  override def run(): Unit = {
    import java.io.BufferedReader
    val inreader = new InputStreamReader(System.in)
    //val i = new Nothing(inreader, System.out, System.err, true)
    var cb = CharBuffer.allocate(2048)

    def eval(ct:Int):Boolean = {
      if (ct<=0) {
        false
      } else {
        val oin = String.valueOf(cb.array()).substring(0, ct)
        val in = oin.replace("\n","")
        if (in!="exit") {
          val args = in.split(" ").toArray
          val c = new CommandLine(new CliCommand())
          c.execute(args: _*)
          cb = CharBuffer.allocate(2048)
          true
        } else {false}
      }

    }

    try {
      val in = new BufferedReader(inreader)

      while ({print("#>> ");val ct = in.read(cb); eval(ct)}) {
      }
      in.close()
    } catch {
      case e: Exception =>

    }
  }
}
