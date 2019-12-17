package io.qpointz.flow.excel

import org.apache.poi.ss.usermodel.Sheet
import WorkbookMethods._


trait RegionSelector {}
case class SelectAllRows() extends RegionSelector

case class RecordSelector (
  tag:String,
  sheets : SheetSelector,
  region : RegionSelector)
