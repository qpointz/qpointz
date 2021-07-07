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

import io.qpointz.flow.text.TextWriterSettings
import io.qpointz.flow.text.csv.CsvFormat.asCsvFormat

object CsvRecordWriterSettings {
  lazy val default: CsvRecordWriterSettings = CsvRecordWriterSettings()


  def asCsvWriterSettings(s: CsvRecordWriterSettings): com.univocity.parsers.csv.CsvWriterSettings = {
    val tg = new com.univocity.parsers.csv.CsvWriterSettings()
    tg.setFormat(if (s.format.isEmpty) tg.getFormat else asCsvFormat(s.format.get))
    tg.setEmptyValue(s.emptyValue.getOrElse(tg.getEmptyValue))
    tg.setEscapeUnquotedValues(s.escapeUnquotedValues.getOrElse(tg.isEscapeUnquotedValues))
    tg.setNormalizeLineEndingsWithinQuotes(s.normalizeLineEndingsWithinQuotes.getOrElse(tg.isNormalizeLineEndingsWithinQuotes))
    tg.setErrorContentLength(s.errorContentLength.getOrElse(tg.getErrorContentLength))
    tg.setNullValue(s.nullValue.getOrElse(tg.getNullValue))
    tg.setMaxCharsPerColumn(s.maxCharsPerColumn.getOrElse(tg.getMaxCharsPerColumn))
    tg.setMaxColumns(s.maxColumns.getOrElse(tg.getMaxColumns))
    tg.setSkipEmptyLines(s.skipEmptyLines.getOrElse(tg.getSkipEmptyLines))
    tg.setIgnoreTrailingWhitespaces(s.ignoreTrailingWhitespaces.getOrElse(tg.getIgnoreTrailingWhitespaces))
    tg.setIgnoreLeadingWhitespaces(s.ignoreLeadingWhitespaces.getOrElse(tg.getIgnoreLeadingWhitespaces))
    tg.setHeaders(s.headers.getOrElse(tg.getHeaders): _*)
    tg.setSkipBitsAsWhitespace(s.skipBitsAsWhitespace.getOrElse(tg.getSkipBitsAsWhitespace))
    tg.setColumnReorderingEnabled(s.columnReorderingEnabled.getOrElse(tg.isColumnReorderingEnabled))
    tg
  }


}

case class CsvRecordWriterSettings(
                                    format: Option[CsvFormat] = None,
                                    emptyValue: Option[String] = None,
                                    escapeUnquotedValues: Option[Boolean] = None,
                                    normalizeLineEndingsWithinQuotes: Option[Boolean] = None,
                                    errorContentLength: Option[Int] = None,
                                    nullValue: Option[String] = None,
                                    maxCharsPerColumn: Option[Int] = None,
                                    maxColumns: Option[Int] = None,
                                    skipEmptyLines: Option[Boolean] = None,
                                    ignoreTrailingWhitespaces: Option[Boolean] = None,
                                    ignoreLeadingWhitespaces: Option[Boolean] = None,
                                    headers: Option[Array[String]] = None,
                                    skipBitsAsWhitespace: Option[Boolean] = None,
                                    columnReorderingEnabled: Option[Boolean] = None,
                                  ) extends TextWriterSettings {

  def format(format: CsvFormat): CsvRecordWriterSettings = {
    copy(format = Some(format))
  }

  def defaultFormat(): CsvRecordWriterSettings = {
    copy(format = None)
  }

  def emptyValue(emptyValue: String): CsvRecordWriterSettings = {
    copy(emptyValue = Some(emptyValue))
  }

  def defaultEmptyValue(): CsvRecordWriterSettings = {
    copy(emptyValue = None)
  }

  def escapeUnquotedValues(escapeUnquotedValues: Boolean): CsvRecordWriterSettings = {
    copy(escapeUnquotedValues = Some(escapeUnquotedValues))
  }

  def defaultEscapeUnquotedValues(): CsvRecordWriterSettings = {
    copy(escapeUnquotedValues = None)
  }

  def normalizeLineEndingsWithinQuotes(normalizeLineEndingsWithinQuotes: Boolean): CsvRecordWriterSettings = {
    copy(normalizeLineEndingsWithinQuotes = Some(normalizeLineEndingsWithinQuotes))
  }

  def defaultNormalizeLineEndingsWithinQuotes(): CsvRecordWriterSettings = {
    copy(normalizeLineEndingsWithinQuotes = None)
  }

  def errorContentLength(errorContentLength: Int): CsvRecordWriterSettings = {
    copy(errorContentLength = Some(errorContentLength))
  }

  def defaultErrorContentLength(): CsvRecordWriterSettings = {
    copy(errorContentLength = None)
  }

  def nullValue(nullValue: String): CsvRecordWriterSettings = {
    copy(nullValue = Some(nullValue))
  }

  def defaultNullValue(): CsvRecordWriterSettings = {
    copy(nullValue = None)
  }

  def maxCharsPerColumn(maxCharsPerColumn: Int): CsvRecordWriterSettings = {
    copy(maxCharsPerColumn = Some(maxCharsPerColumn))
  }

  def defaultMaxCharsPerColumn(): CsvRecordWriterSettings = {
    copy(maxCharsPerColumn = None)
  }

  def maxColumns(maxColumns: Int): CsvRecordWriterSettings = {
    copy(maxColumns = Some(maxColumns))
  }

  def defaultMaxColumns(): CsvRecordWriterSettings = {
    copy(maxColumns = None)
  }

  def skipEmptyLines(skipEmptyLines: Boolean): CsvRecordWriterSettings = {
    copy(skipEmptyLines = Some(skipEmptyLines))
  }

  def defaultSkipEmptyLines(): CsvRecordWriterSettings = {
    copy(skipEmptyLines = None)
  }

  def ignoreTrailingWhitespaces(ignoreTrailingWhitespaces: Boolean): CsvRecordWriterSettings = {
    copy(ignoreTrailingWhitespaces = Some(ignoreTrailingWhitespaces))
  }

  def defaultIgnoreTrailingWhitespaces(): CsvRecordWriterSettings = {
    copy(ignoreTrailingWhitespaces = None)
  }

  def ignoreLeadingWhitespaces(ignoreLeadingWhitespaces: Boolean): CsvRecordWriterSettings = {
    copy(ignoreLeadingWhitespaces = Some(ignoreLeadingWhitespaces))
  }

  def defaultIgnoreLeadingWhitespaces(): CsvRecordWriterSettings = {
    copy(ignoreLeadingWhitespaces = None)
  }

  def headers(headers: Array[String]): CsvRecordWriterSettings = {
    copy(headers = Some(headers))
  }

  def defaultHeaders(): CsvRecordWriterSettings = {
    copy(headers = None)
  }

  def skipBitsAsWhitespace(skipBitsAsWhitespace: Boolean): CsvRecordWriterSettings = {
    copy(skipBitsAsWhitespace = Some(skipBitsAsWhitespace))
  }

  def defaultSkipBitsAsWhitespace(): CsvRecordWriterSettings = {
    copy(skipBitsAsWhitespace = None)
  }

  def columnReorderingEnabled(columnReorderingEnabled: Boolean): CsvRecordWriterSettings = {
    copy(columnReorderingEnabled = Some(columnReorderingEnabled))
  }

  def defaultColumnReorderingEnabled(): CsvRecordWriterSettings = {
    copy(columnReorderingEnabled = None)
  }
}