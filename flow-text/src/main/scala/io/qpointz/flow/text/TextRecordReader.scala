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
import io.qpointz.flow.nio.InputStreamSource
import io.qpointz.flow.{MetaEntry, MetaKey, Metadata, MetadataAwareWithId, MetadataGroupOwner, MetadataMethods, MetadataProvider, Record, RecordReader}

case class TextReaderMetadataSettings(
                                       allowReaderMetadata: Option[Boolean] = None,
                                       allowSourceMetadata: Option[Boolean] = None,
                                       allowRecordMetadata: Option[Boolean] = None
                                     ) {


  val readerMetadataAllowed:Boolean = allowReaderMetadata.getOrElse(false)
  val sourceMedataAllowed:Boolean = allowSourceMetadata.getOrElse(false)
  val recordMetadataAllowed:Boolean = allowRecordMetadata.getOrElse(false)

  def allowReaderMetadata(a: Boolean): TextReaderMetadataSettings = copy(allowReaderMetadata = Some(a))

  def defaultReaderMetadata(): TextReaderMetadataSettings = copy(allowReaderMetadata = None)

  def allowSourceMetadata(a: Boolean): TextReaderMetadataSettings = copy(allowSourceMetadata = Some(a))

  def defaultSourceMetadata(): TextReaderMetadataSettings = copy(allowSourceMetadata = None)

  def allowRecordMetadata(a: Boolean): TextReaderMetadataSettings = copy(allowRecordMetadata = Some(a))

  def defaultRecordMetadata(): TextReaderMetadataSettings = copy(allowRecordMetadata = None)
}

trait TextReaderSettings {
  val metadataSettings:TextReaderMetadataSettings
}

import io.qpointz.flow.MetadataMethods._

object TextRecordReader {
  val metadataGroupKey = "formats:text:recordreader"
}

abstract class TextRecordReader[TParser <: AbstractParser[_], TReaderSettings <: TextReaderSettings]
(val stream: InputStreamSource,
 val settings: TReaderSettings
) extends RecordReader with MetadataAwareWithId {

  protected def createParser(settings: TReaderSettings): TParser

  private lazy val textReader = this

  override def iterator: Iterator[Record] = new Iterator[Record] {
    private lazy val reader = stream.reader
    private lazy val parser: TParser = createParser(settings)

    private lazy val iterable = parser.iterate(reader)
    private lazy val rec_iterator = iterable.iterator()

    override def hasNext: Boolean = rec_iterator.hasNext

    val metaSettings = settings.metadataSettings
    
    private val sourceMetadata = if (metaSettings.sourceMedataAllowed) {
      stream.metadata
    } else {
      MetadataMethods.empty
    }

    private val readerMetadata = if (metaSettings.readerMetadataAllowed) {
      textReader.metadata
    } else {
      MetadataMethods.empty
    }

    private val baseMetadata = sourceMetadata ++ readerMetadata
    private val appendMetadata = metaSettings.readerMetadataAllowed || metaSettings.sourceMedataAllowed || metaSettings.recordMetadataAllowed
    private val appendRecordMetadata = metaSettings.recordMetadataAllowed

    override def next(): Record = {
      val context = rec_iterator.getContext

      val recordMetaData: Metadata = if (!appendMetadata) {
        MetadataMethods.empty
      } else {
        if (!appendRecordMetadata) {
          baseMetadata
        } else {
          baseMetadata ++ Seq[MetaEntry[_]](
            (TextRecordReader.metadataGroupKey, "line", context.currentLine()),
            (TextRecordReader.metadataGroupKey, "pos", context.currentChar()),
            (TextRecordReader.metadataGroupKey, "content_length", context.currentParsedContentLength()),
            (TextRecordReader.metadataGroupKey, "content", context.currentParsedContent()),
            (TextRecordReader.metadataGroupKey, "record_idx", context.currentRecord())
          )
        }
      }

      val values = rec_iterator.next()
      val keys = context.headers()

      Record(keys, values, recordMetaData)
    }
  }
}