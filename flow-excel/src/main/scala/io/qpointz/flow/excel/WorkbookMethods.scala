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