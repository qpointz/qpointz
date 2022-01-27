/*
 * Copyright 2021 qpointz.io
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

package io.qpointz.flow.streams.scaladsl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, RunnableGraph, Sink, Source}
import io.qpointz.flow.{OperationContext, Record, RecordReader, RecordWriter}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

class FlowMethodsTest extends akka.testkit.TestKit(ActorSystem("FlowMethodsTest_Spec")) with AnyFlatSpecLike with Matchers {

  behavior of "from reader"

  it should "run graph" in {

    val reader = RecordReader.fromIterable(List(
      Record("a" -> 1, "b" -> 1),
      Record("a" -> 2, "b" -> 2),
      Record("a" -> 3, "b" -> 3)
    ))



    def writer(buffer:ListBuffer[Record])(implicit ctxp:OperationContext): RecordWriter = new RecordWriter {

      override implicit val ctx: OperationContext = ctxp

      override def open(): Unit = {
        println("open")
      }

      override def close(): Unit = {
        println("close")
      }

      override def write(r: Record): Unit = {
        println(s"write ${r}")
        buffer.append(r)
      }


    }

    val recs = ListBuffer.empty[Record]
    val w = writer(recs)
    val source = FlowMethods.sourceFromReader(reader)
    val r = source.to(FlowMethods.syncToWriter(w)).run()
    println("done")

  }

}
