package io.qpointz.flow.excel

import io.qpointz.flow.data.{MetadataGroupKey, MetadataItem, MetadataKey, MetadataValue}
import org.apache.poi.ss.SpreadsheetVersion

object ExcelMetadata {

  val groupKey: MetadataGroupKey = "excel"

  def workbookPath(path: String): (MetadataGroupKey, MetadataKey, MetadataValue) = item("workbook:path", path)

  def workbookSource(source: String): (MetadataGroupKey, MetadataKey, MetadataValue) = item("workbook:source", source)

  def workbookVersion(spreadsheetVersion: SpreadsheetVersion): MetadataItem = item("workbook:version", spreadsheetVersion.toString)

  def item(key: MetadataKey, value: MetadataValue): MetadataItem = (groupKey, key, value)

  def sheetIndex(idx: Int): MetadataItem = item("sheet:index", idx)

  def sheetName(name: String): MetadataItem = item("sheet:name", name)

  def rowIndex(idx:Int):MetadataItem = item("row:index", idx)

  def recordLabel(label:String):MetadataItem = item("record:label", label)

  def recordTags(tags:Set[String]):MetadataItem = item("record:tags", tags)
}