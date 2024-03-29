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
 *  limitations under the License
 */

package io.qpointz.flow.text.fwf

import com.univocity.parsers.fixed.FixedWidthParser
import io.qpointz.flow.nio.InputStreamSource
import io.qpointz.flow.text.TextRecordReader
import io.qpointz.flow.{Metadata, MetadataMethods, OperationContext, QTypeId, flowQuids}

class FwfRecordReader(stream:InputStreamSource,
                      settings:FwfRecordReaderSettings = FwfFormats.default)
  extends TextRecordReader[FixedWidthParser,FwfRecordReaderSettings](stream, settings)
{
  override val metaId: QTypeId = flowQuids.reader("fwf")

  override protected def createParser(settings: FwfRecordReaderSettings): FixedWidthParser = {
    new FixedWidthParser(settings.asParserSettings)
  }

  override val metadata: Metadata = MetadataMethods.empty
}
