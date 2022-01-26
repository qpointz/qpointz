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

import java.io.{File, FileInputStream, InputStream}

import org.apache.poi.ss.usermodel._

object WorkbookMethods {

  def open(path:String):Workbook = {
    open(new File(path))
  }

  def open(f:File):Workbook = {
    open(new FileInputStream(f))
  }

  def open(is:InputStream):Workbook = {
    WorkbookFactory.create(is)
  }

  implicit class WorkbookExt(wb:Workbook) {
    def sheets():Seq[Sheet] = {
      (0 until wb.getNumberOfSheets).map{x=>
        wb.getSheetAt(x)
      }
    }

  }

  implicit class SheetMethods(sh:Sheet) {
    lazy val workbook: Workbook = sh.getWorkbook
    lazy val index: Int = workbook.getSheetIndex(sh.getSheetName)
    lazy val name: String = sh.getSheetName
  }
}
