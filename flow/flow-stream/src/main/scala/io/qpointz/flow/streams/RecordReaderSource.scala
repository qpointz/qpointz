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

import akka.stream.scaladsl.Source
import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{AbstractOutHandler, GraphStage, GraphStageLogic}
import io.qpointz.flow.{Record, RecordReader}

class RecordReaderSource(private val reader:RecordReader) extends GraphStage[SourceShape[Record]] {

  val out:Outlet[Record] = Outlet.create("RecordReaderSource.out")

  override def shape: SourceShape[Record] = SourceShape.of(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    private val iter = reader.iterator
    setHandler(out, new AbstractOutHandler {
      override def onPull(): Unit = {
        if (iter.hasNext) {
          push(out, iter.next())
        } else {
          complete(out)
        }
      }
    })
  }
}
