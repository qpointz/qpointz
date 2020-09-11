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

package io.qpointz.flow.text

import com.univocity.parsers.common.AbstractParser
import io.qpointz.flow.{MetaEntry, MetaKey, Metadata, MetadataGroupOwner, MetadataMethods, MetadataProvider, Record, RecordReader}


abstract class TextReaderSettings {
  var includeReaderMetadata:Boolean = false
  var includeSourceMetadata:Boolean = false
  var includeRecordMetadata:Boolean = false
}

import io.qpointz.flow.MetadataMethods._

object TextRecordReader {
  val metadataGroupKey = "formats:text:recordreader"
}

abstract class TextRecordReader[TParser<:AbstractParser[_],TReaderSettings <: TextReaderSettings]
                      ( protected val source: TextSource,
                        protected val settings: TReaderSettings
                      ) extends RecordReader with MetadataProvider with MetadataGroupOwner  {

  protected def createParser(settings: TReaderSettings):TParser

  private lazy val textReader = this

  override def iterator: Iterator[Record] = new Iterator[Record] {
    private lazy val reader = source.asReader()
    private lazy val parser:TParser = createParser(settings)

    private lazy val iterable = parser.iterate(reader)
    private lazy val rec_iterator = iterable.iterator()

    override def hasNext: Boolean = rec_iterator.hasNext

    private val sourceMetadata = if (settings.includeSourceMetadata) {
      source.metadata
    } else {
      MetadataMethods.empty
    }

    private val readerMetadata = if (settings.includeReaderMetadata) {
      textReader.metadata
    } else {
      MetadataMethods.empty
    }

    private val baseMetadata = sourceMetadata ++ readerMetadata
    private val appendMetadata = settings.includeSourceMetadata || settings.includeReaderMetadata || settings.includeRecordMetadata
    private val appendRecordMetadata = settings.includeRecordMetadata

    override def next(): Record = {
      val values = rec_iterator.next()
      val context = rec_iterator.getContext
      val keys = context.headers()

      val recordMetaData:Metadata = if (!appendMetadata) {
        MetadataMethods.empty
      } else {
        if (!appendRecordMetadata) {
          baseMetadata
        } else {
          baseMetadata ++ Seq[MetaEntry[_]](
            (TextRecordReader.metadataGroupKey, "line", context.currentLine()),
            (TextRecordReader.metadataGroupKey, "pos" , context.currentChar()),
            (TextRecordReader.metadataGroupKey, "content_length", context.currentParsedContentLength()),
            (TextRecordReader.metadataGroupKey, "content", context.currentParsedContent()),
            (TextRecordReader.metadataGroupKey, "record_idx", context.currentRecord())
          )
        }
      }

      Record(keys, values, recordMetaData)
    }
  }
}