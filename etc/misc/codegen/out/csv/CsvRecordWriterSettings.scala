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

    object CsvRecordWriterSettings {
      lazy val default:CsvRecordWriterSettings = CsvRecordWriterSettings()

        
        def asCsvWriterSettings(s:CsvRecordWriterSettings):com.univocity.parsers.csv.CsvWriterSettings = {
            val tg = new com.univocity.parsers.csv.CsvWriterSettings()
            tg.setEmptyValue(s.emptyValue.getOrElse(tg.getEmptyValue))
            tg.setIgnoreLeadingWhitespacesInQuotes(s.ignoreLeadingWhitespacesInQuotes.getOrElse(tg.getIgnoreLeadingWhitespacesInQuotes))
            tg.setIgnoreTrailingWhitespacesInQuotes(s.ignoreTrailingWhitespacesInQuotes.getOrElse(tg.getIgnoreTrailingWhitespacesInQuotes))
            tg.setEscapeUnquotedValues(s.escapeUnquotedValues.getOrElse(tg.isEscapeUnquotedValues))
            tg.setKeepEscapeSequences(s.keepEscapeSequences.getOrElse(tg.isKeepEscapeSequences))
            tg.setKeepQuotes(s.keepQuotes.getOrElse(tg.getKeepQuotes))
            tg.setNormalizeLineEndingsWithinQuotes(s.normalizeLineEndingsWithinQuotes.getOrElse(tg.isNormalizeLineEndingsWithinQuotes))
            tg.setCommentCollectionEnabled(s.commentCollectionEnabled.getOrElse(tg.isCommentCollectionEnabled))
            tg.setUnescapedQuoteHandling(s.unescapedQuoteHandling.getOrElse(tg.getUnescapedQuoteHandling))
            tg.setErrorContentLength(s.errorContentLength.getOrElse(tg.getErrorContentLength))
            tg.setNullValue(s.nullValue.getOrElse(tg.getNullValue))
            tg.setMaxCharsPerColumn(s.maxCharsPerColumn.getOrElse(tg.getMaxCharsPerColumn))
            tg.setMaxColumns(s.maxColumns.getOrElse(tg.getMaxColumns))
            tg.setSkipEmptyLines(s.skipEmptyLines.getOrElse(tg.getSkipEmptyLines))
            tg.setIgnoreTrailingWhitespaces(s.ignoreTrailingWhitespaces.getOrElse(tg.getIgnoreTrailingWhitespaces))
            tg.setIgnoreLeadingWhitespaces(s.ignoreLeadingWhitespaces.getOrElse(tg.getIgnoreLeadingWhitespaces))
            tg.setHeaders(s.headers.getOrElse(tg.getHeaders))
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

    case class CsvRecordWriterSettings(
      emptyValue:Option[String] = None,
      ignoreLeadingWhitespacesInQuotes:Option[Boolean] = None,
      ignoreTrailingWhitespacesInQuotes:Option[Boolean] = None,
      escapeUnquotedValues:Option[Boolean] = None,
      keepEscapeSequences:Option[Boolean] = None,
      keepQuotes:Option[Boolean] = None,
      normalizeLineEndingsWithinQuotes:Option[Boolean] = None,
      commentCollectionEnabled:Option[Boolean] = None,
      unescapedQuoteHandling:Option[UnescapedQuoteHandling] = None,
      errorContentLength:Option[Int] = None,
      nullValue:Option[String] = None,
      maxCharsPerColumn:Option[Int] = None,
      maxColumns:Option[Int] = None,
      skipEmptyLines:Option[Boolean] = None,
      ignoreTrailingWhitespaces:Option[Boolean] = None,
      ignoreLeadingWhitespaces:Option[Boolean] = None,
      headers:Option[Array[String]] = None,
      skipBitsAsWhitespace:Option[Boolean] = None,
      headerExtractionEnabled:Option[Boolean] = None,
      columnReorderingEnabled:Option[Boolean] = None,
      inputBufferSize:Option[Int] = None,
      numberOfRecordsToRead:Option[Long] = None,
      lineSeparatorDetectionEnabled:Option[Boolean] = None,
      numberOfRowsToSkip:Option[Long] = None,
    )  extends TextWriterSettings  {
        
        def emptyValue(emptyValue:String): CsvRecordWriterSettings = {copy(emptyValue=Some(emptyValue))}
        def defaultEmptyValue():CsvRecordWriterSettings = {copy(emptyValue=None)}
        
        def ignoreLeadingWhitespacesInQuotes(ignoreLeadingWhitespacesInQuotes:Boolean): CsvRecordWriterSettings = {copy(ignoreLeadingWhitespacesInQuotes=Some(ignoreLeadingWhitespacesInQuotes))}
        def defaultIgnoreLeadingWhitespacesInQuotes():CsvRecordWriterSettings = {copy(ignoreLeadingWhitespacesInQuotes=None)}
        
        def ignoreTrailingWhitespacesInQuotes(ignoreTrailingWhitespacesInQuotes:Boolean): CsvRecordWriterSettings = {copy(ignoreTrailingWhitespacesInQuotes=Some(ignoreTrailingWhitespacesInQuotes))}
        def defaultIgnoreTrailingWhitespacesInQuotes():CsvRecordWriterSettings = {copy(ignoreTrailingWhitespacesInQuotes=None)}
        
        def escapeUnquotedValues(escapeUnquotedValues:Boolean): CsvRecordWriterSettings = {copy(escapeUnquotedValues=Some(escapeUnquotedValues))}
        def defaultEscapeUnquotedValues():CsvRecordWriterSettings = {copy(escapeUnquotedValues=None)}
        
        def keepEscapeSequences(keepEscapeSequences:Boolean): CsvRecordWriterSettings = {copy(keepEscapeSequences=Some(keepEscapeSequences))}
        def defaultKeepEscapeSequences():CsvRecordWriterSettings = {copy(keepEscapeSequences=None)}
        
        def keepQuotes(keepQuotes:Boolean): CsvRecordWriterSettings = {copy(keepQuotes=Some(keepQuotes))}
        def defaultKeepQuotes():CsvRecordWriterSettings = {copy(keepQuotes=None)}
        
        def normalizeLineEndingsWithinQuotes(normalizeLineEndingsWithinQuotes:Boolean): CsvRecordWriterSettings = {copy(normalizeLineEndingsWithinQuotes=Some(normalizeLineEndingsWithinQuotes))}
        def defaultNormalizeLineEndingsWithinQuotes():CsvRecordWriterSettings = {copy(normalizeLineEndingsWithinQuotes=None)}
        
        def commentCollectionEnabled(commentCollectionEnabled:Boolean): CsvRecordWriterSettings = {copy(commentCollectionEnabled=Some(commentCollectionEnabled))}
        def defaultCommentCollectionEnabled():CsvRecordWriterSettings = {copy(commentCollectionEnabled=None)}
        
        def unescapedQuoteHandling(unescapedQuoteHandling:UnescapedQuoteHandling): CsvRecordWriterSettings = {copy(unescapedQuoteHandling=Some(unescapedQuoteHandling))}
        def defaultUnescapedQuoteHandling():CsvRecordWriterSettings = {copy(unescapedQuoteHandling=None)}
        
        def errorContentLength(errorContentLength:Int): CsvRecordWriterSettings = {copy(errorContentLength=Some(errorContentLength))}
        def defaultErrorContentLength():CsvRecordWriterSettings = {copy(errorContentLength=None)}
        
        def nullValue(nullValue:String): CsvRecordWriterSettings = {copy(nullValue=Some(nullValue))}
        def defaultNullValue():CsvRecordWriterSettings = {copy(nullValue=None)}
        
        def maxCharsPerColumn(maxCharsPerColumn:Int): CsvRecordWriterSettings = {copy(maxCharsPerColumn=Some(maxCharsPerColumn))}
        def defaultMaxCharsPerColumn():CsvRecordWriterSettings = {copy(maxCharsPerColumn=None)}
        
        def maxColumns(maxColumns:Int): CsvRecordWriterSettings = {copy(maxColumns=Some(maxColumns))}
        def defaultMaxColumns():CsvRecordWriterSettings = {copy(maxColumns=None)}
        
        def skipEmptyLines(skipEmptyLines:Boolean): CsvRecordWriterSettings = {copy(skipEmptyLines=Some(skipEmptyLines))}
        def defaultSkipEmptyLines():CsvRecordWriterSettings = {copy(skipEmptyLines=None)}
        
        def ignoreTrailingWhitespaces(ignoreTrailingWhitespaces:Boolean): CsvRecordWriterSettings = {copy(ignoreTrailingWhitespaces=Some(ignoreTrailingWhitespaces))}
        def defaultIgnoreTrailingWhitespaces():CsvRecordWriterSettings = {copy(ignoreTrailingWhitespaces=None)}
        
        def ignoreLeadingWhitespaces(ignoreLeadingWhitespaces:Boolean): CsvRecordWriterSettings = {copy(ignoreLeadingWhitespaces=Some(ignoreLeadingWhitespaces))}
        def defaultIgnoreLeadingWhitespaces():CsvRecordWriterSettings = {copy(ignoreLeadingWhitespaces=None)}
        
        def headers(headers:Array[String]): CsvRecordWriterSettings = {copy(headers=Some(headers))}
        def defaultHeaders():CsvRecordWriterSettings = {copy(headers=None)}
        
        def skipBitsAsWhitespace(skipBitsAsWhitespace:Boolean): CsvRecordWriterSettings = {copy(skipBitsAsWhitespace=Some(skipBitsAsWhitespace))}
        def defaultSkipBitsAsWhitespace():CsvRecordWriterSettings = {copy(skipBitsAsWhitespace=None)}
        
        def headerExtractionEnabled(headerExtractionEnabled:Boolean): CsvRecordWriterSettings = {copy(headerExtractionEnabled=Some(headerExtractionEnabled))}
        def defaultHeaderExtractionEnabled():CsvRecordWriterSettings = {copy(headerExtractionEnabled=None)}
        
        def columnReorderingEnabled(columnReorderingEnabled:Boolean): CsvRecordWriterSettings = {copy(columnReorderingEnabled=Some(columnReorderingEnabled))}
        def defaultColumnReorderingEnabled():CsvRecordWriterSettings = {copy(columnReorderingEnabled=None)}
        
        def inputBufferSize(inputBufferSize:Int): CsvRecordWriterSettings = {copy(inputBufferSize=Some(inputBufferSize))}
        def defaultInputBufferSize():CsvRecordWriterSettings = {copy(inputBufferSize=None)}
        
        def numberOfRecordsToRead(numberOfRecordsToRead:Long): CsvRecordWriterSettings = {copy(numberOfRecordsToRead=Some(numberOfRecordsToRead))}
        def defaultNumberOfRecordsToRead():CsvRecordWriterSettings = {copy(numberOfRecordsToRead=None)}
        
        def lineSeparatorDetectionEnabled(lineSeparatorDetectionEnabled:Boolean): CsvRecordWriterSettings = {copy(lineSeparatorDetectionEnabled=Some(lineSeparatorDetectionEnabled))}
        def defaultLineSeparatorDetectionEnabled():CsvRecordWriterSettings = {copy(lineSeparatorDetectionEnabled=None)}
        
        def numberOfRowsToSkip(numberOfRowsToSkip:Long): CsvRecordWriterSettings = {copy(numberOfRowsToSkip=Some(numberOfRowsToSkip))}
        def defaultNumberOfRowsToSkip():CsvRecordWriterSettings = {copy(numberOfRowsToSkip=None)}
        
}