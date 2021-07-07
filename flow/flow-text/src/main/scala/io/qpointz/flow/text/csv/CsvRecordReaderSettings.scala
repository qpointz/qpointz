/*
 * Copyright 2021 qpointz.io
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

package io.qpointz.flow.text.csv

import io.qpointz.flow.text.csv.CsvFormat.asCsvFormat
import io.qpointz.flow.text.{TextReaderMetadataSettings, TextReaderSettings}

object CsvRecordReaderSettings {
  lazy val default: CsvRecordReaderSettings = CsvRecordReaderSettings()

  def asCsvParserSettings(s: CsvRecordReaderSettings): com.univocity.parsers.csv.CsvParserSettings = {
    val tg = new com.univocity.parsers.csv.CsvParserSettings()
    tg.setFormat(if (s.format.isEmpty) tg.getFormat else asCsvFormat(s.format.get))
    tg.setEmptyValue(s.emptyValue.getOrElse(tg.getEmptyValue))
    tg.setIgnoreLeadingWhitespacesInQuotes(s.ignoreLeadingWhitespacesInQuotes.getOrElse(tg.getIgnoreLeadingWhitespacesInQuotes))
    tg.setIgnoreTrailingWhitespacesInQuotes(s.ignoreTrailingWhitespacesInQuotes.getOrElse(tg.getIgnoreTrailingWhitespacesInQuotes))
    tg.setEscapeUnquotedValues(s.escapeUnquotedValues.getOrElse(tg.isEscapeUnquotedValues))
    tg.setKeepEscapeSequences(s.keepEscapeSequences.getOrElse(tg.isKeepEscapeSequences))
    tg.setKeepQuotes(s.keepQuotes.getOrElse(tg.getKeepQuotes))
    tg.setNormalizeLineEndingsWithinQuotes(s.normalizeLineEndingsWithinQuotes.getOrElse(tg.isNormalizeLineEndingsWithinQuotes))
    tg.setCommentCollectionEnabled(s.commentCollectionEnabled.getOrElse(tg.isCommentCollectionEnabled))
    tg.setUnescapedQuoteHandling(if (s.unescapedQuoteHandling.isEmpty) tg.getUnescapedQuoteHandling else s.unescapedQuoteHandling.get.orig)
    tg.setErrorContentLength(s.errorContentLength.getOrElse(tg.getErrorContentLength))
    tg.setNullValue(s.nullValue.getOrElse(tg.getNullValue))
    tg.setMaxCharsPerColumn(s.maxCharsPerColumn.getOrElse(tg.getMaxCharsPerColumn))
    tg.setMaxColumns(s.maxColumns.getOrElse(tg.getMaxColumns))
    tg.setSkipEmptyLines(s.skipEmptyLines.getOrElse(tg.getSkipEmptyLines))
    tg.setIgnoreTrailingWhitespaces(s.ignoreTrailingWhitespaces.getOrElse(tg.getIgnoreTrailingWhitespaces))
    tg.setIgnoreLeadingWhitespaces(s.ignoreLeadingWhitespaces.getOrElse(tg.getIgnoreLeadingWhitespaces))
    tg.setHeaders(s.headers.getOrElse(tg.getHeaders): _*)
    tg.setSkipBitsAsWhitespace(s.skipBitsAsWhitespace.getOrElse(tg.getSkipBitsAsWhitespace))
    tg.setHeaderExtractionEnabled(s.headerExtractionEnabled.getOrElse(tg.isHeaderExtractionEnabled))
    tg.setColumnReorderingEnabled(s.columnReorderingEnabled.getOrElse(tg.isColumnReorderingEnabled))
    tg.setInputBufferSize(s.inputBufferSize.getOrElse(tg.getInputBufferSize))
    tg.setNumberOfRecordsToRead(s.numberOfRecordsToRead.getOrElse(tg.getNumberOfRecordsToRead))
    tg.setLineSeparatorDetectionEnabled(s.lineSeparatorDetectionEnabled.getOrElse(tg.isLineSeparatorDetectionEnabled))
    tg.setNumberOfRowsToSkip(s.numberOfRowsToSkip.getOrElse(tg.getNumberOfRowsToSkip))
    tg
  }
}

case class CsvRecordReaderSettings(
    format: Option[CsvFormat] = None,
    emptyValue: Option[String] = None,
    ignoreLeadingWhitespacesInQuotes: Option[Boolean] = None,
    ignoreTrailingWhitespacesInQuotes: Option[Boolean] = None,
    escapeUnquotedValues: Option[Boolean] = None,
    keepEscapeSequences: Option[Boolean] = None,
    keepQuotes: Option[Boolean] = None,
    normalizeLineEndingsWithinQuotes: Option[Boolean] = None,
    commentCollectionEnabled: Option[Boolean] = None,
    unescapedQuoteHandling: Option[UnescapedQuoteHandling.Val] = None,
    errorContentLength: Option[Int] = None,
    nullValue: Option[String] = None,
    maxCharsPerColumn: Option[Int] = None,
    maxColumns: Option[Int] = None,
    skipEmptyLines: Option[Boolean] = None,
    ignoreTrailingWhitespaces: Option[Boolean] = None,
    ignoreLeadingWhitespaces: Option[Boolean] = None,
    headers: Option[Array[String]] = None,
    skipBitsAsWhitespace: Option[Boolean] = None,
    headerExtractionEnabled: Option[Boolean] = None,
    columnReorderingEnabled: Option[Boolean] = None,
    inputBufferSize: Option[Int] = None,
    numberOfRecordsToRead: Option[Long] = None,
    lineSeparatorDetectionEnabled: Option[Boolean] = None,
    numberOfRowsToSkip: Option[Long] = None,
    metadataSettings: TextReaderMetadataSettings = TextReaderMetadataSettings()
  ) extends TextReaderSettings {

  def format(format: CsvFormat): CsvRecordReaderSettings = {
    copy(format = Some(format))
  }

  def defaultFormat(): CsvRecordReaderSettings = {
    copy(format = None)
  }

  def emptyValue(emptyValue: String): CsvRecordReaderSettings = {
    copy(emptyValue = Some(emptyValue))
  }

  def defaultEmptyValue(): CsvRecordReaderSettings = {
    copy(emptyValue = None)
  }

  def ignoreLeadingWhitespacesInQuotes(ignoreLeadingWhitespacesInQuotes: Boolean): CsvRecordReaderSettings = {
    copy(ignoreLeadingWhitespacesInQuotes = Some(ignoreLeadingWhitespacesInQuotes))
  }

  def defaultIgnoreLeadingWhitespacesInQuotes(): CsvRecordReaderSettings = {
    copy(ignoreLeadingWhitespacesInQuotes = None)
  }

  def ignoreTrailingWhitespacesInQuotes(ignoreTrailingWhitespacesInQuotes: Boolean): CsvRecordReaderSettings = {
    copy(ignoreTrailingWhitespacesInQuotes = Some(ignoreTrailingWhitespacesInQuotes))
  }

  def defaultIgnoreTrailingWhitespacesInQuotes(): CsvRecordReaderSettings = {
    copy(ignoreTrailingWhitespacesInQuotes = None)
  }

  def escapeUnquotedValues(escapeUnquotedValues: Boolean): CsvRecordReaderSettings = {
    copy(escapeUnquotedValues = Some(escapeUnquotedValues))
  }

  def defaultEscapeUnquotedValues(): CsvRecordReaderSettings = {
    copy(escapeUnquotedValues = None)
  }

  def keepEscapeSequences(keepEscapeSequences: Boolean): CsvRecordReaderSettings = {
    copy(keepEscapeSequences = Some(keepEscapeSequences))
  }

  def defaultKeepEscapeSequences(): CsvRecordReaderSettings = {
    copy(keepEscapeSequences = None)
  }

  def keepQuotes(keepQuotes: Boolean): CsvRecordReaderSettings = {
    copy(keepQuotes = Some(keepQuotes))
  }

  def defaultKeepQuotes(): CsvRecordReaderSettings = {
    copy(keepQuotes = None)
  }

  def normalizeLineEndingsWithinQuotes(normalizeLineEndingsWithinQuotes: Boolean): CsvRecordReaderSettings = {
    copy(normalizeLineEndingsWithinQuotes = Some(normalizeLineEndingsWithinQuotes))
  }

  def defaultNormalizeLineEndingsWithinQuotes(): CsvRecordReaderSettings = {
    copy(normalizeLineEndingsWithinQuotes = None)
  }

  def commentCollectionEnabled(commentCollectionEnabled: Boolean): CsvRecordReaderSettings = {
    copy(commentCollectionEnabled = Some(commentCollectionEnabled))
  }

  def defaultCommentCollectionEnabled(): CsvRecordReaderSettings = {
    copy(commentCollectionEnabled = None)
  }

  def unescapedQuoteHandling(unescapedQuoteHandling: UnescapedQuoteHandling.Val): CsvRecordReaderSettings = {
    copy(unescapedQuoteHandling = Some(unescapedQuoteHandling))
  }

  def defaultUnescapedQuoteHandling(): CsvRecordReaderSettings = {
    copy(unescapedQuoteHandling = None)
  }

  def errorContentLength(errorContentLength: Int): CsvRecordReaderSettings = {
    copy(errorContentLength = Some(errorContentLength))
  }

  def defaultErrorContentLength(): CsvRecordReaderSettings = {
    copy(errorContentLength = None)
  }

  def nullValue(nullValue: String): CsvRecordReaderSettings = {
    copy(nullValue = Some(nullValue))
  }

  def defaultNullValue(): CsvRecordReaderSettings = {
    copy(nullValue = None)
  }

  def maxCharsPerColumn(maxCharsPerColumn: Int): CsvRecordReaderSettings = {
    copy(maxCharsPerColumn = Some(maxCharsPerColumn))
  }

  def defaultMaxCharsPerColumn(): CsvRecordReaderSettings = {
    copy(maxCharsPerColumn = None)
  }

  def maxColumns(maxColumns: Int): CsvRecordReaderSettings = {
    copy(maxColumns = Some(maxColumns))
  }

  def defaultMaxColumns(): CsvRecordReaderSettings = {
    copy(maxColumns = None)
  }

  def skipEmptyLines(skipEmptyLines: Boolean): CsvRecordReaderSettings = {
    copy(skipEmptyLines = Some(skipEmptyLines))
  }

  def defaultSkipEmptyLines(): CsvRecordReaderSettings = {
    copy(skipEmptyLines = None)
  }

  def ignoreTrailingWhitespaces(ignoreTrailingWhitespaces: Boolean): CsvRecordReaderSettings = {
    copy(ignoreTrailingWhitespaces = Some(ignoreTrailingWhitespaces))
  }

  def defaultIgnoreTrailingWhitespaces(): CsvRecordReaderSettings = {
    copy(ignoreTrailingWhitespaces = None)
  }

  def ignoreLeadingWhitespaces(ignoreLeadingWhitespaces: Boolean): CsvRecordReaderSettings = {
    copy(ignoreLeadingWhitespaces = Some(ignoreLeadingWhitespaces))
  }

  def defaultIgnoreLeadingWhitespaces(): CsvRecordReaderSettings = {
    copy(ignoreLeadingWhitespaces = None)
  }

  def headers(headers: Array[String]): CsvRecordReaderSettings = {
    copy(headers = Some(headers))
  }

  def defaultHeaders(): CsvRecordReaderSettings = {
    copy(headers = None)
  }

  def skipBitsAsWhitespace(skipBitsAsWhitespace: Boolean): CsvRecordReaderSettings = {
    copy(skipBitsAsWhitespace = Some(skipBitsAsWhitespace))
  }

  def defaultSkipBitsAsWhitespace(): CsvRecordReaderSettings = {
    copy(skipBitsAsWhitespace = None)
  }

  def headerExtractionEnabled(headerExtractionEnabled: Boolean): CsvRecordReaderSettings = {
    copy(headerExtractionEnabled = Some(headerExtractionEnabled))
  }

  def defaultHeaderExtractionEnabled(): CsvRecordReaderSettings = {
    copy(headerExtractionEnabled = None)
  }

  def columnReorderingEnabled(columnReorderingEnabled: Boolean): CsvRecordReaderSettings = {
    copy(columnReorderingEnabled = Some(columnReorderingEnabled))
  }

  def defaultColumnReorderingEnabled(): CsvRecordReaderSettings = {
    copy(columnReorderingEnabled = None)
  }

  def inputBufferSize(inputBufferSize: Int): CsvRecordReaderSettings = {
    copy(inputBufferSize = Some(inputBufferSize))
  }

  def defaultInputBufferSize(): CsvRecordReaderSettings = {
    copy(inputBufferSize = None)
  }

  def numberOfRecordsToRead(numberOfRecordsToRead: Long): CsvRecordReaderSettings = {
    copy(numberOfRecordsToRead = Some(numberOfRecordsToRead))
  }

  def defaultNumberOfRecordsToRead(): CsvRecordReaderSettings = {
    copy(numberOfRecordsToRead = None)
  }

  def lineSeparatorDetectionEnabled(lineSeparatorDetectionEnabled: Boolean): CsvRecordReaderSettings = {
    copy(lineSeparatorDetectionEnabled = Some(lineSeparatorDetectionEnabled))
  }

  def defaultLineSeparatorDetectionEnabled(): CsvRecordReaderSettings = {
    copy(lineSeparatorDetectionEnabled = None)
  }

  def numberOfRowsToSkip(numberOfRowsToSkip: Long): CsvRecordReaderSettings = {
    copy(numberOfRowsToSkip = Some(numberOfRowsToSkip))
  }

  def defaultNumberOfRowsToSkip(): CsvRecordReaderSettings = {
    copy(numberOfRowsToSkip = None)
  }

}