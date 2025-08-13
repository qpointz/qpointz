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

object CsvFormat {
  lazy val default: CsvFormat = CsvFormat()

  def asCsvFormat(s: CsvFormat): com.univocity.parsers.csv.CsvFormat = {
    val tg = new com.univocity.parsers.csv.CsvFormat()
    tg.setComment(s.comment.getOrElse(tg.getComment))
    tg.setLineSeparator(s.lineSeparator.getOrElse(tg.getLineSeparatorString))
    tg.setNormalizedNewline(s.normalizedNewline.getOrElse(tg.getNormalizedNewline))
    tg.setDelimiter(s.delimiter.getOrElse(tg.getDelimiterString))
    tg.setQuote(s.quote.getOrElse(tg.getQuote))
    tg.setQuoteEscape(s.quoteEscape.getOrElse(tg.getQuoteEscape))
    tg.setCharToEscapeQuoteEscaping(s.charToEscapeQuoteEscaping.getOrElse(tg.getCharToEscapeQuoteEscaping))
    tg
  }
}

case class CsvFormat(
                      comment: Option[Char] = None,
                      lineSeparator: Option[String] = None,
                      normalizedNewline: Option[Char] = None,
                      delimiter: Option[String] = None,
                      quote: Option[Char] = None,
                      quoteEscape: Option[Char] = None,
                      charToEscapeQuoteEscaping: Option[Char] = None
                    ) {

  def comment(comment: Char): CsvFormat = {
    copy(comment = Some(comment))
  }

  def defaultComment(): CsvFormat = {
    copy(comment = None)
  }

  def lineSeparator(lineSeparator: String): CsvFormat = {
    copy(lineSeparator = Some(lineSeparator))
  }

  def defaultLineSeparator(): CsvFormat = {
    copy(lineSeparator = None)
  }

  def normalizedNewline(normalizedNewline: Char): CsvFormat = {
    copy(normalizedNewline = Some(normalizedNewline))
  }

  def defaultNormalizedNewline(): CsvFormat = {
    copy(normalizedNewline = None)
  }

  def delimiter(delimiter: String): CsvFormat = {
    copy(delimiter = Some(delimiter))
  }

  def defaultDelimiter(): CsvFormat = {
    copy(delimiter = None)
  }

  def quote(quote: Char): CsvFormat = {
    copy(quote = Some(quote))
  }

  def defaultQuote(): CsvFormat = {
    copy(quote = None)
  }

  def quoteEscape(quoteEscape: Char): CsvFormat = {
    copy(quoteEscape = Some(quoteEscape))
  }

  def defaultQuoteEscape(): CsvFormat = {
    copy(quoteEscape = None)
  }

  def charToEscapeQuoteEscaping(charToEscapeQuoteEscaping: Char): CsvFormat = {
    copy(charToEscapeQuoteEscaping = Some(charToEscapeQuoteEscaping))
  }

  def defaultCharToEscapeQuoteEscaping(): CsvFormat = {
    copy(charToEscapeQuoteEscaping = None)
  }
}