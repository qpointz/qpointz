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

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{IOOperationIncompleteException, IOResult}
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.qpointz.flow.{InMemoryWriter, Record}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{Await, Future}
import akka.stream.scaladsl._
import akka.stream.testkit.scaladsl._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

class RecordWriterStageTest extends akka.testkit.TestKit(ActorSystem("MySpec"))
  with Matchers
  with AnyFlatSpecLike
  with ScalaFutures {

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

    val a = Source.fromIterator(()=>fi).runWith(new RecordWriterStage(new InMemoryWriter()))
    import scala.concurrent.ExecutionContext.Implicits.global
    val rd = Await.ready(a, 3.seconds)
      .onComplete(x=> x match {
        case Failure(exception:IOOperationIncompleteException) => {
          exception.count shouldBe 10
        }
        case _ => assert(false)
    })
  }

}
