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

    object FwfRecordWriterSettings {
      lazy val default:FwfRecordWriterSettings = FwfRecordWriterSettings()

        
        def asFwfParserSettings(s:FwfRecordWriterSettings):com.univocity.parsers.csv.FixedWidthWriterSettings = {
            val tg = new com.univocity.parsers.csv.FixedWidthWriterSettings()
            tg.(s.fields.getOrElse(tg.))
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

    case class FwfRecordWriterSettings(
      fields:Option[Map[String,Integer]] = None,
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
        
        def fields(fields:Map[String,Integer]): FwfRecordWriterSettings = {copy(fields=Some(fields))}
        def defaultFields():FwfRecordWriterSettings = {copy(fields=None)}
        
        def nullValue(nullValue:String): FwfRecordWriterSettings = {copy(nullValue=Some(nullValue))}
        def defaultNullValue():FwfRecordWriterSettings = {copy(nullValue=None)}
        
        def maxCharsPerColumn(maxCharsPerColumn:Int): FwfRecordWriterSettings = {copy(maxCharsPerColumn=Some(maxCharsPerColumn))}
        def defaultMaxCharsPerColumn():FwfRecordWriterSettings = {copy(maxCharsPerColumn=None)}
        
        def maxColumns(maxColumns:Int): FwfRecordWriterSettings = {copy(maxColumns=Some(maxColumns))}
        def defaultMaxColumns():FwfRecordWriterSettings = {copy(maxColumns=None)}
        
        def skipEmptyLines(skipEmptyLines:Boolean): FwfRecordWriterSettings = {copy(skipEmptyLines=Some(skipEmptyLines))}
        def defaultSkipEmptyLines():FwfRecordWriterSettings = {copy(skipEmptyLines=None)}
        
        def ignoreTrailingWhitespaces(ignoreTrailingWhitespaces:Boolean): FwfRecordWriterSettings = {copy(ignoreTrailingWhitespaces=Some(ignoreTrailingWhitespaces))}
        def defaultIgnoreTrailingWhitespaces():FwfRecordWriterSettings = {copy(ignoreTrailingWhitespaces=None)}
        
        def ignoreLeadingWhitespaces(ignoreLeadingWhitespaces:Boolean): FwfRecordWriterSettings = {copy(ignoreLeadingWhitespaces=Some(ignoreLeadingWhitespaces))}
        def defaultIgnoreLeadingWhitespaces():FwfRecordWriterSettings = {copy(ignoreLeadingWhitespaces=None)}
        
        def headers(headers:Array[String]): FwfRecordWriterSettings = {copy(headers=Some(headers))}
        def defaultHeaders():FwfRecordWriterSettings = {copy(headers=None)}
        
        def skipBitsAsWhitespace(skipBitsAsWhitespace:Boolean): FwfRecordWriterSettings = {copy(skipBitsAsWhitespace=Some(skipBitsAsWhitespace))}
        def defaultSkipBitsAsWhitespace():FwfRecordWriterSettings = {copy(skipBitsAsWhitespace=None)}
        
        def headerExtractionEnabled(headerExtractionEnabled:Boolean): FwfRecordWriterSettings = {copy(headerExtractionEnabled=Some(headerExtractionEnabled))}
        def defaultHeaderExtractionEnabled():FwfRecordWriterSettings = {copy(headerExtractionEnabled=None)}
        
        def columnReorderingEnabled(columnReorderingEnabled:Boolean): FwfRecordWriterSettings = {copy(columnReorderingEnabled=Some(columnReorderingEnabled))}
        def defaultColumnReorderingEnabled():FwfRecordWriterSettings = {copy(columnReorderingEnabled=None)}
        
        def inputBufferSize(inputBufferSize:Int): FwfRecordWriterSettings = {copy(inputBufferSize=Some(inputBufferSize))}
        def defaultInputBufferSize():FwfRecordWriterSettings = {copy(inputBufferSize=None)}
        
        def numberOfRecordsToRead(numberOfRecordsToRead:Long): FwfRecordWriterSettings = {copy(numberOfRecordsToRead=Some(numberOfRecordsToRead))}
        def defaultNumberOfRecordsToRead():FwfRecordWriterSettings = {copy(numberOfRecordsToRead=None)}
        
        def lineSeparatorDetectionEnabled(lineSeparatorDetectionEnabled:Boolean): FwfRecordWriterSettings = {copy(lineSeparatorDetectionEnabled=Some(lineSeparatorDetectionEnabled))}
        def defaultLineSeparatorDetectionEnabled():FwfRecordWriterSettings = {copy(lineSeparatorDetectionEnabled=None)}
        
        def numberOfRowsToSkip(numberOfRowsToSkip:Long): FwfRecordWriterSettings = {copy(numberOfRowsToSkip=Some(numberOfRowsToSkip))}
        def defaultNumberOfRowsToSkip():FwfRecordWriterSettings = {copy(numberOfRowsToSkip=None)}
        
}