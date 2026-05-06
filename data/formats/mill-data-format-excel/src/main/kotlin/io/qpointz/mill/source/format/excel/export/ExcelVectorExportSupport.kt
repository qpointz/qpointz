package io.qpointz.mill.source.format.excel.export

import io.qpointz.mill.sql.RecordReader
import io.qpointz.mill.sql.RecordReaders
import io.qpointz.mill.vectors.VectorBlockIterator
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import java.io.OutputStream
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Executes [block] then invokes [RecordReader.close]; [RecordReader] does not implement [AutoCloseable].
 */
private inline fun <R> RecordReader.useReader(block: (RecordReader) -> R): R {
    try {
        return block(this)
    } finally {
        close()
    }
}

/**
 * XLSX export over [VectorBlockIterator] using a row-windowed workbook (bounded POI memory).
 */
internal object ExcelVectorExportSupport {

    private const val DEFAULT_ROW_WINDOW = 100

    /**
     * @param iterator vector batch source
     * @param out Excel OOXML stream
     */
    fun writeXlsx(iterator: VectorBlockIterator, out: OutputStream) {
        SXSSFWorkbook(DEFAULT_ROW_WINDOW).use { workbook ->
            val sheet = workbook.createSheet("Export")
            var excelRow = 0
            RecordReaders.recordReader(iterator).useReader { rr ->
                if (!rr.hasNext()) {
                    return@useReader
                }
                val header = sheet.createRow(excelRow++)
                for (i in 0 until rr.columnCount) {
                    header.createCell(i).setCellValue(rr.getColumnMetadata(i).name)
                }
                while (rr.hasNext()) {
                    rr.next()
                    val row = sheet.createRow(excelRow++)
                    for (i in 0 until rr.columnCount) {
                        val cell = row.createCell(i)
                        if (rr.isNull(i)) {
                            cell.setBlank()
                            continue
                        }
                        val v = rr.getObject(i)
                        when (v) {
                            is Boolean -> cell.setCellValue(v)
                            is Double -> cell.setCellValue(v)
                            is Float -> cell.setCellValue(v.toDouble())
                            is Int -> cell.setCellValue(v.toDouble())
                            is Long -> cell.setCellValue(v.toDouble())
                            is Short -> cell.setCellValue(v.toDouble())
                            is Byte -> cell.setCellValue(v.toDouble())
                            is BigDecimal -> cell.setCellValue(v.toDouble())
                            is BigInteger -> cell.setCellValue(v.toDouble())
                            else -> cell.setCellValue(v.toString())
                        }
                    }
                }
            }
            workbook.write(out)
        }
    }
}
