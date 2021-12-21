/*
 * Copyright 2020 qpointz.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.qpointz.flow

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait RecordWriter extends WithOperationContext {

  def open(): Unit

  def close(): Unit

  def write(r: Record)

}

class InMemoryWriter(implicit val ctx:OperationContext) extends RecordWriter {

  private var maybeOpened : Option[Boolean]= None
  private val recs:ListBuffer[Record] = ListBuffer[Record]()

  override def open(): Unit = {
    maybeOpened = Some(true)
  }

  override def close(): Unit = {
    maybeOpened = Some(false)
  }

  override def write(r: Record): Unit = {
    recs.append(r)
  }

  def records():Seq[Record] = recs.toSeq

  def isClosed:Boolean = maybeOpened.contains(false)

  def isOpened:Boolean = maybeOpened.contains(true)

}

object RecordWriter {

  def inMemory(implicit ctx:OperationContext) : InMemoryWriter =  new InMemoryWriter()

}