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

package io.qpointz.flow.streams

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Source, _}
import akka.stream.{Graph, IOOperationIncompleteException, IOResult, KillSwitches}
import io.qpointz.flow.{InMemoryWriter, OperationContext, Record, RecordWriter}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class RecordWriterStageTest extends akka.testkit.TestKit(ActorSystem("MySpec"))
  with AsyncFlatSpecLike
  with Matchers
  with ScalaFutures {

  behavior of "RecordWriterStage"

  it should "write records" in {
    val inm = new InMemoryWriter()
    val sink = Sink.fromGraph(new RecordWriterStage(inm))
    val source = Source.fromIterator(()=>ratesRecords.iterator)
    val a:Future[IOResult] = source.runWith(sink)
    val res = Await.result(a, 5.seconds)
    inm.records().length shouldBe res.count
    res.count shouldBe ratesRecordsReader.toSeq.length
    inm.isClosed shouldBe true
  }

  it should "handle ustream failure" in {
    val fi = new Iterator[Record] {
      val src = ratesRecords.iterator
      var prc = 10
      override def hasNext: Boolean = src.hasNext
      override def next(): Record = {
        prc -= 1
        if (prc<0) {
          throw new RuntimeException("Failed")
        }
        src.next()
      }
    }

    recoverToExceptionIf[IOOperationIncompleteException] {
      Source.fromIterator(() => fi).runWith(new RecordWriterStage(new InMemoryWriter()))
    }.map {ex=>
      ex.count shouldBe 10
    }

  }

  it should "fail if writer cannot be opened" in {
    val wrt = new RecordWriter {
      override def open(): Unit = {
        throw new RuntimeException("Failed to open")
      }
      override def close(): Unit = {}
      override def write(r: Record): Unit = {}
    }
    recoverToExceptionIf[IOOperationIncompleteException] {
      Source.fromIterator(()=>ratesRecords.iterator).runWith(new RecordWriterStage(wrt))
    }.map { ex=>
      ex.count shouldBe 0
    }
  }

  it should "fail if writer cannot be closed" in {
    val wrt = new RecordWriter {
      override def open(): Unit = {}
      override def close(): Unit = {throw new RuntimeException("Failed to close")}
      override def write(r: Record): Unit = {}
    }
    recoverToExceptionIf[RuntimeException] {
      Source.fromIterator(()=>ratesRecords.iterator).runWith(new RecordWriterStage(wrt))
    }.map {ex=>
      ex.getMessage shouldBe "Failed to close"
    }
  }

  it should "fail if writer cannot write" in {
    val wrt = new RecordWriter {
      override def open(): Unit = {}
      override def close(): Unit = {}
      var i = 0
      override def write(r: Record): Unit = {
        if (i>=10) {
          throw new RuntimeException("Failed to push")
        }
        i+=1
      }
    }
    recoverToExceptionIf[IOOperationIncompleteException] {
      Source.fromIterator(()=>ratesRecords.iterator).runWith(new RecordWriterStage(wrt))
    }.map {ex=>
      ex.count shouldBe 10
      ex.getCause.getMessage shouldBe  "Failed to push"
    }
  }
}
