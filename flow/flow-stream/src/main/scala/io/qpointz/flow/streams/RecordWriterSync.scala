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

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler}
import akka.stream.{Attributes, Inlet, SinkShape}
import io.qpointz.flow.{Record, RecordWriter}

class RecordWriterSync(private val writer:RecordWriter) extends GraphStage[SinkShape[Record]] {

  val in:Inlet[Record] = Inlet.create("RecordWriterSink.in")

  override def shape: SinkShape[Record] = SinkShape.of(in)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    override def preStart(): Unit = {
      writer.open()
      pull(in)
    }

    override def postStop(): Unit = {
      writer.close()
    }

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val r = grab(in)
        writer.write(r)
        pull(in)
      }
    })
  }
}
