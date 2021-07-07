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

import com.univocity.parsers.fixed.FixedWidthParserSettings

class FwfRecordReaderSettings extends TextReaderSettings {

  var NormalizedNewline: Char = _

  var LineSeparator:Array[Char] = _

  var Comment: Char = _

  var Padding: Char = _

  var SkipEmptyLines: Boolean = _

  var SkipBitsAsWhitespace: Boolean = _

  var NumberOfRowsToSkip: Long = _

  var NumberOfRecordsToRead: Long = _

  var NullValue: String = _

  var MaxColumns: Int = _

  var MaxCharsPerColumn: Int = _

  var LineSeparatorDetectionEnabled: Boolean = _

  var IgnoreTrailingWhitespaces: Boolean = _

  var IgnoreLeadingWhitespaces: Boolean = _

  var Headers: Array[String] = _

  var HeaderExtractionEnabled: Boolean = _

  var ErrorContentLength: Int = _

  var CommentCollectionEnabled: Boolean = _

  var ColumnReorderingEnabled: Boolean = _

  var AutoConfigurationEnabled: Boolean = _

  var AutoClosingEnabled: Boolean = _

  var UseDefaultPaddingForHeaders: Boolean = _

  var SkipTrailingCharsUntilNewline: Boolean = _

  var KeepPadding: Boolean = _

  var RecordEndsOnNewline: Boolean=_

  override val metadataSettings:TextReaderMetadataSettings = TextReaderMetadataSettings()

  fromParserSettings(new FixedWidthParserSettings())

  def asParserSettings: FixedWidthParserSettings = {
    val cs = new FixedWidthParserSettings()
    cs.setKeepPadding(this.KeepPadding)
    cs.setRecordEndsOnNewline(this.RecordEndsOnNewline)
    cs.setSkipTrailingCharsUntilNewline(this.SkipTrailingCharsUntilNewline)
    cs.setUseDefaultPaddingForHeaders(this.UseDefaultPaddingForHeaders)
    cs.setAutoClosingEnabled(this.AutoClosingEnabled)
    cs.setAutoConfigurationEnabled(this.AutoConfigurationEnabled)
    cs.setColumnReorderingEnabled(this.ColumnReorderingEnabled)
    cs.setCommentCollectionEnabled(this.CommentCollectionEnabled)
    cs.setErrorContentLength(this.ErrorContentLength)
    cs.setHeaderExtractionEnabled(this.HeaderExtractionEnabled)
    cs.setHeaders(this.Headers:_*)
    cs.setIgnoreLeadingWhitespaces(this.IgnoreLeadingWhitespaces)
    cs.setIgnoreTrailingWhitespaces(this.IgnoreTrailingWhitespaces)
    cs.setLineSeparatorDetectionEnabled(this.LineSeparatorDetectionEnabled)
    cs.setMaxCharsPerColumn(this.MaxCharsPerColumn)
    cs.setMaxColumns(this.MaxColumns)
    cs.setNullValue(this.NullValue)
    cs.setNumberOfRecordsToRead(this.NumberOfRecordsToRead)
    cs.setNumberOfRowsToSkip(this.NumberOfRowsToSkip)
    cs.setSkipBitsAsWhitespace(this.SkipBitsAsWhitespace)
    cs.setSkipEmptyLines(this.SkipEmptyLines)
    val fmt = cs.getFormat
    fmt.setPadding(this.Padding)
    fmt.setComment(this.Comment)
    fmt.setLineSeparator(this.LineSeparator)
    fmt.setNormalizedNewline(this.NormalizedNewline)
    cs
  }

  def fromParserSettings(cs:FixedWidthParserSettings):Unit = {
    this.KeepPadding=cs.getKeepPadding
    this.RecordEndsOnNewline=cs.getRecordEndsOnNewline
    this.SkipTrailingCharsUntilNewline=cs.getSkipTrailingCharsUntilNewline
    this.UseDefaultPaddingForHeaders=cs.getUseDefaultPaddingForHeaders
    this.AutoClosingEnabled=cs.isAutoClosingEnabled
    this.AutoConfigurationEnabled=cs.isAutoConfigurationEnabled
    this.ColumnReorderingEnabled=cs.isColumnReorderingEnabled
    this.CommentCollectionEnabled=cs.isCommentCollectionEnabled
    this.ErrorContentLength=cs.getErrorContentLength
    this.HeaderExtractionEnabled=cs.isHeaderExtractionEnabled
    this.Headers=cs.getHeaders
    this.IgnoreLeadingWhitespaces=cs.getIgnoreLeadingWhitespaces
    this.IgnoreTrailingWhitespaces=cs.getIgnoreTrailingWhitespaces
    this.LineSeparatorDetectionEnabled=cs.isLineSeparatorDetectionEnabled
    this.MaxCharsPerColumn=cs.getMaxCharsPerColumn
    this.MaxColumns=cs.getMaxColumns
    this.NullValue=cs.getNullValue
    this.NumberOfRecordsToRead=cs.getNumberOfRecordsToRead
    this.NumberOfRowsToSkip=cs.getNumberOfRowsToSkip
    this.SkipBitsAsWhitespace=cs.getSkipBitsAsWhitespace
    this.SkipEmptyLines=cs.getSkipEmptyLines
    val fmt =  cs.getFormat
    this.Padding=fmt.getPadding
    this.Comment=fmt.getComment
    this.LineSeparator=fmt.getLineSeparator
    this.NormalizedNewline=fmt.getNormalizedNewline
  }

}
