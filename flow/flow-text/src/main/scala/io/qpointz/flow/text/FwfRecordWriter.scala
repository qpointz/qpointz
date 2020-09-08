/*
 * Copyright 2020 qpointz.io
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.qpointz.flow.text

import com.univocity.parsers.fixed.{FixedWidthFormat, FixedWidthWriter, FixedWidthWriterSettings}
import io.qpointz.flow.OperationContext

class FwfRecordWriterSettings extends TextWriterSettings {

  var padding: Char = _

  var comment: Char = _

  var normalizedNewLine:Char = _

  var lineSeparator:String = _

  var emptyValue:String = _

  var headerWritingEnabled:Boolean = _

  def asWriterSettings(): FixedWidthWriterSettings = {
    val fwws = new FixedWidthWriterSettings()
    val fmt = new FixedWidthFormat()
    fmt.setPadding(this.padding)
    fmt.setComment(this.comment)
    fmt.setNormalizedNewline(this.normalizedNewLine)
    fmt.setLineSeparator(this.lineSeparator)
    fwws.setFormat(fmt)
    fwws.setEmptyValue(this.emptyValue)
    fwws.setHeaderWritingEnabled(this.headerWritingEnabled)
    fwws
  }
}

class FwfRecordWriter(settings: FwfRecordWriterSettings)(implicit val ctx:OperationContext)
  extends TextRecordWriter[FixedWidthWriter,FwfRecordWriterSettings](settings) {

  override protected def createWriter(settings: FwfRecordWriterSettings): FixedWidthWriter = {
    new FixedWidthWriter(settings.asWriterSettings())
  }
  
}
