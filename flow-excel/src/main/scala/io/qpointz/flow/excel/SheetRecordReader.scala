package io.qpointz.flow.excel

import io.qpointz.flow.data.{AttributeKey, AttributeValue, Metadata, Record, RecordReader, RecordTags}
import org.apache.poi.ss.usermodel.{Cell, CellType, DateUtil, Row, Sheet}

import scala.jdk.CollectionConverters._

class SheetRecordReaderSettings {
  var recordLabel:String = _
  var noneValue: AttributeValue  = AttributeValue.Null
  var blankValue: AttributeValue = AttributeValue.Empty
  var errorValue: AttributeValue = AttributeValue.Error
  var columns:SheetColumnCollection = _
}

class SheetRecordReader(val sheet:Sheet,
                        val settings: SheetRecordReaderSettings,
                        val extraMetadata:Metadata
                       ) extends RecordReader {

  import ExcelMetadata._
  import WorkbookMethods._

  private def cellValue(c:Cell):AttributeValue = {
    @scala.annotation.tailrec
    def byType(ct:CellType):AttributeValue = ct match {
      case CellType.NUMERIC if DateUtil.isCellDateFormatted(c) => c.getDateCellValue
      case CellType.NUMERIC => c.getNumericCellValue
      case CellType.BOOLEAN => c.getBooleanCellValue
      case CellType.STRING => c.getStringCellValue
      case CellType.FORMULA if c.getCellFormula.startsWith("TRUE") => true
      case CellType.FORMULA if c.getCellFormula.startsWith("FALSE") => false
      case CellType.FORMULA => byType(c.getCachedFormulaResultType)
      case CellType._NONE => settings.noneValue
      case CellType.BLANK => settings.blankValue
      case CellType.ERROR => settings.errorValue
      case _ => throw new IllegalArgumentException(s"Unknown CellType ${ct}")
    }
    byType(c.getCellType)
  }

  def iterator: Iterator[Record] = {
    val columns = settings.columns.colIndexMap()

    val sheetMetadata = extraMetadata ++ Seq(
      sheetIndex(sheet.index),
      sheetName(sheet.name))

    val colKeys = columns.keys.toSet

    def toRecord(r:Row):Record = {
      val cells = r.cellIterator().asScala
          .map(c=> (c.getColumnIndex, c))
          .toMap

      val cellKeys = cells.keys.toSet

      val columnvalues = (cellKeys ++ colKeys)
        .map(x=> {(cells.get(x), columns.get(x)) match {
          case (Some(cell), Some(col)) => (col.header, cellValue(cell), Set())
          case (None, Some(col)) => (col.header, AttributeValue.Missing, Set(RecordTags.MissingValue))
          case (Some(cell), None) => (s"Column_${x}", cellValue(cell), Set(RecordTags.UnexpectedValue))
          case _ => throw new IllegalArgumentException(s"Missing column index:${x}")
        }})

      val values = columnvalues
        .map(k=> (k._1, k._2))
        .toMap

      val tags = columnvalues
        .flatMap(_._3)
        .toSet

      val fulltags =  if (tags.isEmpty) {
        Set(RecordTags.OK)
      } else {
        tags + RecordTags.NOK
      }

      val metadata = sheetMetadata :+
        rowIndex(r.getRowNum) :+
        recordLabel(settings.recordLabel) :+
        recordTags(fulltags)

      Record(values, metadata)
    }

    sheet.rowIterator().asScala
      .map(toRecord)
  }

}