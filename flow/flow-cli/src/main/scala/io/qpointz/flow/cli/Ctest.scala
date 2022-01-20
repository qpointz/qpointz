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
 *  limitations under the License
 */

package io.qpointz.flow.cli

import org.apache.commons.lang3.time.StopWatch
import shapeless.syntax.std._
import shapeless._
import shapeless.syntax.std.traversable.traversableOps
import shapeless.syntax.std.tuple.{hlistOps, productTupleOps}

import scala.concurrent.duration.Duration

object Ctest {

  case class Test(i:Int)

  def main(args:Array[String]):Unit = {

    val samples = 1000 * 1000 * 100

    def c1 = {
      System.gc()
      val a = StopWatch.createStarted()
      var b: Test = Test(1)
      for (i <- 1 to samples) {
        val c = Test(i + b.i)
        b = c
      }
        a.stop()
      a
    }

    def hlist = {
      System.gc()
      val sw = StopWatch.createStarted()
      var b: Test = Test(1)
      for (i <- 1 to samples) {
        val a = (1::"mmmm"::4::HNil)
        /** 1000 */
        val c = Test(i + b.i)
        b = c
      }
        sw.stop()
      sw
    }

    def plain = {
      System.gc()
      val a = StopWatch.createStarted()
      var b: Int = 1
      for (i <- 1 to samples) {
        /** 1000 */
        val c = i + b
        b = c
      }
        a.stop()
      a
    }


    val (cs, pl) = (hlist, plain)

    println(s"$cs")
    println(s"$pl")
    println(s"${Duration.fromNanos(cs.getNanoTime-pl.getNanoTime).toSeconds}")

  }

}
