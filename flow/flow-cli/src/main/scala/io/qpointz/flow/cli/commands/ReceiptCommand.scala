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

package io.qpointz.flow.cli.commands


import io.qpointz.flow.receipts.Receipt
import picocli.CommandLine.{Command, Option, Parameters}

import java.io.File
import scala.jdk.CollectionConverters.MapHasAsScala

@Command(name = "receipt")
class ReceiptCommand extends Runnable {

  @Parameters(index = "0")
  var inputFile : File = _

  @Option(names = Array("--prop", "-P"))
  var ps: java.util.Map[String,String] = _

  override def run(): Unit = {
    println(s"file:${inputFile}")
    ps.asScala.foreach(x=>println(s"property ${x._1} :=: ${x._2}"))
    val r = Receipt.fromFile(inputFile, ps.asScala.toMap)
    r.run()
  }
}
