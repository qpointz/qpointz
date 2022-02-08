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
import akka.stream.scaladsl.{Sink, Source}
import io.qpointz.flow.{Record, RecordReader, RecordWriter}
import org.reactivestreams.{Subscriber, Subscription}

object FlowMethods {

  def sourceFromReader(reader:RecordReader):Source[Record,NotUsed] = {
    Source.fromIterator[Record](()=>reader.iterator)
  }

  def syncToWriter(writer:RecordWriter): Sink[Record, NotUsed] = {
     Sink.fromSubscriber(new Subscriber[Record] {
      override def onSubscribe(s: Subscription): Unit = {
        writer.open()
      }

      override def onNext(t: Record): Unit = {
        println(s"record:${t}")
        writer.write(t)
      }

      override def onError(t: Throwable): Unit = {
        println(s"error:${t}")
      }

      override def onComplete(): Unit = {
        writer.close()
      }
    })
  }

}
