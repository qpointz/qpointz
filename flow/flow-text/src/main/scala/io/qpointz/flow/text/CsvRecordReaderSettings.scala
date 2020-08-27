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

import com.univocity.parsers.csv.{CsvParserSettings, UnescapedQuoteHandling}

class CsvRecordReaderSettings extends TextReaderSettings  {

  var charToEscapeQuoteEscaping : Char = _

  var detectFormatAutomatically: Char = _

  var delimiterDetectionEnabled: Boolean = _

  var emptyValue: String = _

  var escapeUnquotedValues: Boolean = _

  var ignoreLeadingWhitespacesInQuotes: Boolean = _

  var ignoreLeadingWhitespaces: Boolean = _

  var ignoreTrailingWhitespacesInQuotes: Boolean = _

  var ignoreTrailingWhitespaces: Boolean = _

  var keepEscapeSequences: Boolean = _

  var keepQuotes: Boolean = _

  var normalizeLineEndingsWithinQuotes: Boolean = _

  var quoteDetectionEnabled: Boolean = _

  var unescapedQuoteHandling: UnescapedQuoteHandling = _

  var autoClosingEnabled: Boolean = _

  var autoConfigurationEnabled: Boolean = _

  var columnReorderingEnabled: Boolean = _

  var commentCollectionEnabled: Boolean = _

  var errorContentLength: Int = _

  var headerExtractionEnabled: Boolean = _

  var headers: Array[String] = _

  var inputBufferSize: Int = _

  var lineSeparatorDetectionEnabled: Boolean = _

  var maxCharsPerColumn: Int = _

  var maxColumns: Int = _

  var nullValue: String = _

  var numberOfRecordsToRead: Long = _

  var numberOfRowsToSkip: Long = _

  var readInputOnSeparateThread: Boolean = _

  var skipBitsAsWhitespace: Boolean = _

  var skipEmptyLines: Boolean = _

  var delimiter:String = _

  var quote: Char = _

  var quoteEscape: Char = _

  var comment: Char = _

  var lineSeparator:Array[Char] = _

  var normalizedNewLine: Char = _

  fromParserSettings(new CsvParserSettings)

  def asParserSettings:CsvParserSettings = {
    val cs = new CsvParserSettings
    cs.detectFormatAutomatically(detectFormatAutomatically)
    cs.setDelimiterDetectionEnabled(delimiterDetectionEnabled)
    cs.setEmptyValue(emptyValue)
    cs.setEscapeUnquotedValues(escapeUnquotedValues)
    cs.setIgnoreLeadingWhitespacesInQuotes(ignoreLeadingWhitespacesInQuotes)
    cs.setIgnoreLeadingWhitespaces(ignoreLeadingWhitespaces)
    cs.setIgnoreTrailingWhitespacesInQuotes(ignoreTrailingWhitespacesInQuotes)
    cs.setIgnoreTrailingWhitespaces(ignoreTrailingWhitespaces)
    cs.setKeepEscapeSequences(keepEscapeSequences)
    cs.setKeepQuotes(keepQuotes)
    cs.setNormalizeLineEndingsWithinQuotes(normalizeLineEndingsWithinQuotes)
    cs.setQuoteDetectionEnabled(quoteDetectionEnabled)
    cs.setUnescapedQuoteHandling(unescapedQuoteHandling)
    cs.setAutoClosingEnabled(autoClosingEnabled)
    cs.setAutoConfigurationEnabled(autoConfigurationEnabled)
    cs.setColumnReorderingEnabled(columnReorderingEnabled)
    cs.setCommentCollectionEnabled(commentCollectionEnabled)
    cs.setErrorContentLength(errorContentLength)
    cs.setHeaderExtractionEnabled(headerExtractionEnabled)
    cs.setHeaders(headers:_*)
    cs.setInputBufferSize(inputBufferSize)
    cs.setLineSeparatorDetectionEnabled(lineSeparatorDetectionEnabled)
    cs.setMaxCharsPerColumn(maxCharsPerColumn)
    cs.setMaxColumns(maxColumns)
    cs.setNullValue(nullValue)
    cs.setNumberOfRecordsToRead(numberOfRecordsToRead)
    cs.setNumberOfRowsToSkip(numberOfRowsToSkip)
    cs.setReadInputOnSeparateThread(readInputOnSeparateThread)
    cs.setSkipBitsAsWhitespace(skipBitsAsWhitespace)
    cs.setSkipEmptyLines(skipEmptyLines)
    val fmt = cs.getFormat
    fmt.setCharToEscapeQuoteEscaping(charToEscapeQuoteEscaping)
    fmt.setDelimiter(delimiter)
    fmt.setQuote(quote)
    fmt.setQuoteEscape(quoteEscape)
    fmt.setComment(comment)
    fmt.setLineSeparator(lineSeparator)
    fmt.setNormalizedNewline(normalizedNewLine)
    cs
  }

  def fromParserSettings(csd:CsvParserSettings):Unit = {
      this.delimiterDetectionEnabled = csd.isDelimiterDetectionEnabled
      this.emptyValue = csd.getEmptyValue
      this.escapeUnquotedValues = csd.isEscapeUnquotedValues
      this.ignoreLeadingWhitespacesInQuotes = csd.getIgnoreLeadingWhitespacesInQuotes
      this.ignoreLeadingWhitespaces = csd.getIgnoreLeadingWhitespaces
      this.ignoreTrailingWhitespacesInQuotes = csd.getIgnoreTrailingWhitespacesInQuotes
      this.ignoreTrailingWhitespaces = csd.getIgnoreTrailingWhitespaces
      this.keepEscapeSequences = csd.isKeepEscapeSequences
      this.keepQuotes = csd.getKeepQuotes
      this.normalizeLineEndingsWithinQuotes = csd.isNormalizeLineEndingsWithinQuotes
      this.quoteDetectionEnabled = csd.isQuoteDetectionEnabled
      this.unescapedQuoteHandling = csd.getUnescapedQuoteHandling
      this.autoClosingEnabled = csd.isAutoClosingEnabled
      this.autoConfigurationEnabled = csd.isAutoConfigurationEnabled
      this.columnReorderingEnabled = csd.isColumnReorderingEnabled
      this.commentCollectionEnabled = csd.isCommentCollectionEnabled
      this.errorContentLength = csd.getErrorContentLength
      this.headerExtractionEnabled = csd.isHeaderExtractionEnabled
      this.headers = csd.getHeaders
      this.inputBufferSize = csd.getInputBufferSize
      this.lineSeparatorDetectionEnabled = csd.isLineSeparatorDetectionEnabled
      this.maxCharsPerColumn = csd.getMaxCharsPerColumn
      this.maxColumns = csd.getMaxColumns
      this.nullValue = csd.getNullValue
      this.numberOfRecordsToRead = csd.getNumberOfRecordsToRead
      this.numberOfRowsToSkip = csd.getNumberOfRowsToSkip
      this.readInputOnSeparateThread = csd.getReadInputOnSeparateThread
      this.skipBitsAsWhitespace = csd.getSkipBitsAsWhitespace
      this.skipEmptyLines = csd.getSkipEmptyLines

      val fmt = csd.getFormat

      this.delimiter = fmt.getDelimiterString
      this.charToEscapeQuoteEscaping = fmt.getCharToEscapeQuoteEscaping
      this.quote = fmt.getQuote
      this.quoteEscape = fmt.getQuoteEscape
      this.comment = fmt.getComment
      this.lineSeparator = fmt.getLineSeparator
      this.normalizedNewLine = fmt.getNormalizedNewline
  }

}