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
import io.qpointz.flow.{OperationContext, Record, RecordReader}
import org.scalatest.flatspec.{AnyFlatSpec, AnyFlatSpecLike}
import org.scalatest.matchers.should.Matchers
import akka.stream._
import akka.stream.scaladsl._
import akka.stream.testkit.scaladsl._

class RecordReaderSourceTest extends akka.testkit.TestKit(ActorSystem("MySpec")) with Matchers with AnyFlatSpecLike {

  implicit val ctx: OperationContext = OperationContext.defaultContext

  val records = Seq(
    Record("a" -> 1, "b" -> 1),
    Record("a" -> 2, "b" -> 2),
    Record("a" -> 3, "b" -> 3)
  )

  def testRecords(): RecordReader = RecordReader.fromIterable(records)

  it should "complete" in {
    val srx = Source.fromGraph(new RecordReaderSource(testRecords))
    srx.runWith(TestSink[Record]())
      .request(4)
      .expectNext(Record("a" -> 1, "b" -> 1))
      .expectNext(Record("a" -> 2, "b" -> 2))
      .expectNext(Record("a" -> 3, "b" -> 3))
      .expectComplete()

  }



}
