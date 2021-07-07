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

package io.qpointz.flow.text.fwf

    object FwfRecordReaderSettings {
      lazy val default:FwfRecordReaderSettings = FwfRecordReaderSettings()

        
        def asFwfParserSettings(s:FwfRecordReaderSettings):com.univocity.parsers.csv.FixedWidthParserSettings = {
            val tg = new com.univocity.parsers.csv.FixedWidthParserSettings()
            tg.setRecordsEndsOnNewLine(s.recordsEndsOnNewLine.getOrElse(tg.getRecordsEndsOnNewLine))
            tg.setSkipTrailingCharsUntilNewline(s.skipTrailingCharsUntilNewline.getOrElse(tg.getSkipTrailingCharsUntilNewline))
            tg.setUseDefaultPaddingForHeaders(s.useDefaultPaddingForHeaders.getOrElse(tg.getUseDefaultPaddingForHeaders))
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

    case class FwfRecordReaderSettings(
      recordsEndsOnNewLine:Option[Boolean] = None,
      skipTrailingCharsUntilNewline:Option[Boolean] = None,
      useDefaultPaddingForHeaders:Option[Boolean] = None,
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
    )  extends TextReaderSettings  {
        
        def recordsEndsOnNewLine(recordsEndsOnNewLine:Boolean): FwfRecordReaderSettings = {copy(recordsEndsOnNewLine=Some(recordsEndsOnNewLine))}
        def defaultRecordsEndsOnNewLine():FwfRecordReaderSettings = {copy(recordsEndsOnNewLine=None)}
        
        def skipTrailingCharsUntilNewline(skipTrailingCharsUntilNewline:Boolean): FwfRecordReaderSettings = {copy(skipTrailingCharsUntilNewline=Some(skipTrailingCharsUntilNewline))}
        def defaultSkipTrailingCharsUntilNewline():FwfRecordReaderSettings = {copy(skipTrailingCharsUntilNewline=None)}
        
        def useDefaultPaddingForHeaders(useDefaultPaddingForHeaders:Boolean): FwfRecordReaderSettings = {copy(useDefaultPaddingForHeaders=Some(useDefaultPaddingForHeaders))}
        def defaultUseDefaultPaddingForHeaders():FwfRecordReaderSettings = {copy(useDefaultPaddingForHeaders=None)}
        
        def nullValue(nullValue:String): FwfRecordReaderSettings = {copy(nullValue=Some(nullValue))}
        def defaultNullValue():FwfRecordReaderSettings = {copy(nullValue=None)}
        
        def maxCharsPerColumn(maxCharsPerColumn:Int): FwfRecordReaderSettings = {copy(maxCharsPerColumn=Some(maxCharsPerColumn))}
        def defaultMaxCharsPerColumn():FwfRecordReaderSettings = {copy(maxCharsPerColumn=None)}
        
        def maxColumns(maxColumns:Int): FwfRecordReaderSettings = {copy(maxColumns=Some(maxColumns))}
        def defaultMaxColumns():FwfRecordReaderSettings = {copy(maxColumns=None)}
        
        def skipEmptyLines(skipEmptyLines:Boolean): FwfRecordReaderSettings = {copy(skipEmptyLines=Some(skipEmptyLines))}
        def defaultSkipEmptyLines():FwfRecordReaderSettings = {copy(skipEmptyLines=None)}
        
        def ignoreTrailingWhitespaces(ignoreTrailingWhitespaces:Boolean): FwfRecordReaderSettings = {copy(ignoreTrailingWhitespaces=Some(ignoreTrailingWhitespaces))}
        def defaultIgnoreTrailingWhitespaces():FwfRecordReaderSettings = {copy(ignoreTrailingWhitespaces=None)}
        
        def ignoreLeadingWhitespaces(ignoreLeadingWhitespaces:Boolean): FwfRecordReaderSettings = {copy(ignoreLeadingWhitespaces=Some(ignoreLeadingWhitespaces))}
        def defaultIgnoreLeadingWhitespaces():FwfRecordReaderSettings = {copy(ignoreLeadingWhitespaces=None)}
        
        def headers(headers:Array[String]): FwfRecordReaderSettings = {copy(headers=Some(headers))}
        def defaultHeaders():FwfRecordReaderSettings = {copy(headers=None)}
        
        def skipBitsAsWhitespace(skipBitsAsWhitespace:Boolean): FwfRecordReaderSettings = {copy(skipBitsAsWhitespace=Some(skipBitsAsWhitespace))}
        def defaultSkipBitsAsWhitespace():FwfRecordReaderSettings = {copy(skipBitsAsWhitespace=None)}
        
        def headerExtractionEnabled(headerExtractionEnabled:Boolean): FwfRecordReaderSettings = {copy(headerExtractionEnabled=Some(headerExtractionEnabled))}
        def defaultHeaderExtractionEnabled():FwfRecordReaderSettings = {copy(headerExtractionEnabled=None)}
        
        def columnReorderingEnabled(columnReorderingEnabled:Boolean): FwfRecordReaderSettings = {copy(columnReorderingEnabled=Some(columnReorderingEnabled))}
        def defaultColumnReorderingEnabled():FwfRecordReaderSettings = {copy(columnReorderingEnabled=None)}
        
        def inputBufferSize(inputBufferSize:Int): FwfRecordReaderSettings = {copy(inputBufferSize=Some(inputBufferSize))}
        def defaultInputBufferSize():FwfRecordReaderSettings = {copy(inputBufferSize=None)}
        
        def numberOfRecordsToRead(numberOfRecordsToRead:Long): FwfRecordReaderSettings = {copy(numberOfRecordsToRead=Some(numberOfRecordsToRead))}
        def defaultNumberOfRecordsToRead():FwfRecordReaderSettings = {copy(numberOfRecordsToRead=None)}
        
        def lineSeparatorDetectionEnabled(lineSeparatorDetectionEnabled:Boolean): FwfRecordReaderSettings = {copy(lineSeparatorDetectionEnabled=Some(lineSeparatorDetectionEnabled))}
        def defaultLineSeparatorDetectionEnabled():FwfRecordReaderSettings = {copy(lineSeparatorDetectionEnabled=None)}
        
        def numberOfRowsToSkip(numberOfRowsToSkip:Long): FwfRecordReaderSettings = {copy(numberOfRowsToSkip=Some(numberOfRowsToSkip))}
        def defaultNumberOfRowsToSkip():FwfRecordReaderSettings = {copy(numberOfRowsToSkip=None)}
        
}