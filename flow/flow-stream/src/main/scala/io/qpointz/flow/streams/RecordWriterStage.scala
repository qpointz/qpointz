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

import akka.stream.scaladsl.FileIO
import akka.stream.stage.{AbstractInHandler, GraphStage, GraphStageLogic, GraphStageWithMaterializedValue, InHandler}
import akka.stream.{AbruptStageTerminationException, Attributes, IOOperationIncompleteException, IOResult, Inlet, SinkShape}
import io.qpointz.flow.{Record, RecordWriter}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}
import scala.util.control.NonFatal

class RecordWriterStage(private val writer:RecordWriter)
  extends GraphStageWithMaterializedValue[SinkShape[Record], Future[IOResult]] {

  val in:Inlet[Record] = Inlet.create("RecordWriterSink.in")

  override def shape: SinkShape[Record] = SinkShape.of(in)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[IOResult]) = {
    val mat = Promise[IOResult]
    val logic = new GraphStageLogic(shape) with InHandler {
      var recordsWritten:Long = 0

      private def closeWriter(s:Option[Throwable]):Unit = {
        try {
          if (writer ne null) writer.close()
          s match {
            case None => mat.tryComplete(Success(IOResult(recordsWritten)))
            case Some(t) => mat.tryFailure(t)
          }
        } catch {
          case NonFatal(ex)=>
            mat.tryFailure(s.getOrElse(ex))
        }
      }

      override def preStart(): Unit = {
        try {
          writer.open()
          pull(in)
        } catch {
          case NonFatal(t) =>
            closeWriter(Some(new IOOperationIncompleteException(recordsWritten, t)))
            failStage(t)
        }
      }

      override def onPush(): Unit = {
        val n = grab(in)
        try {
          writer.write(n)
          recordsWritten+=1
          pull(in)
        } catch {
          case NonFatal(ex) =>
            closeWriter(Some(new IOOperationIncompleteException(recordsWritten, ex)))
            failStage(ex)
        }
      }

      override def onUpstreamFailure(ex: Throwable): Unit = {
        closeWriter(Some(new IOOperationIncompleteException(recordsWritten, ex)))
        failStage(ex)
      }

      override def onUpstreamFinish(): Unit = {
        closeWriter(None)
        completeStage()
      }

      override def postStop(): Unit = {
        if (!mat.isCompleted) {
          val failure = new AbruptStageTerminationException(this)
          closeWriter(Some(failure))
          mat.tryFailure(failure)
        }
      }

      setHandler(in, this)
    }

    (logic, mat.future)
  }
}
