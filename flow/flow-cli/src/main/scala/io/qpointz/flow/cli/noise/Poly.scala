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

package io.qpointz.flow.cli.noise

import org.graalvm.polyglot._


object Polyglot {
  def main(args: Array[String]): Unit = {
    val context = Context.newBuilder("python").allowIO(true).build()
    val array = context.eval("python", "def func(i): return i+10")
    //val result = array.as(classOf[Array[Int]]).toSeq
    System.out.println("result")
  }
}
