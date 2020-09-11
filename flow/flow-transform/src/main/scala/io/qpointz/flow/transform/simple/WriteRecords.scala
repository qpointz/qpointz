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

package io.qpointz.flow.transform.simple

import io.qpointz.flow.{OperationContext, RecordReader, RecordWriter, WithOperationContext}

class WriteRecordsSettings() {
  protected var vOpenWriter = false
  private var vCloseWriter = true

  def openWriter():Boolean = vOpenWriter
  def openWriter(v:Boolean):Unit = {
    vOpenWriter = v
  }

  def closeWriter():Boolean = vCloseWriter
  def closeWriter(v:Boolean):Unit = {
    vCloseWriter = v
  }

}

class WriteRecords(private val reader:RecordReader,
                   private val writer:RecordWriter,
                   private val settings:WriteRecordsSettings)(implicit val ctx:OperationContext)
  extends WithOperationContext {

  def transform(): Unit = {

    if (settings.openWriter()) {
      ctx.log.debug("Opening writer")
      writer.open()
    }

    ctx.log.info("begin read")
    reader.foreach(writer.write)

    if (settings.closeWriter()) {
      ctx.log.debug("Closing writer")
      writer.close()
    }
  }

}
