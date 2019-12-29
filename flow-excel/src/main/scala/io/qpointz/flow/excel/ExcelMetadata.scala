/*
 * Copyright 2019 qpointz.io
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
 */

package io.qpointz.flow.excel

import io.qpointz.flow.data.{Metadata, MetadataItem, MetadataItemOps, MetadataKey, MetadataOps, MetadataValue}
import org.apache.poi.ss.SpreadsheetVersion


object ExcelMetadata extends MetadataOps("excel"){

  implicit class ExcelMetaOps(val m:Metadata) {
    def workbookPath: MetadataItemOps[String] = item[String](m, "workbook:path")
    def workbookSource: MetadataItemOps[String] = item[String](m, "workbook:source")
    def workbookVersion: MetadataItemOps[SpreadsheetVersion] = item[SpreadsheetVersion](m, "workbook:version")
    def sheetIndex: MetadataItemOps[Int] = item[Int](m, "sheet:index")
    def sheetName: MetadataItemOps[String] = item[String](m, "sheet:name")
    def rowIndex: MetadataItemOps[Int] = item[Int](m, "row:index")
  }

}
