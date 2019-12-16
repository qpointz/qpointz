import java.io.{File, FileInputStream}
import org.apache.poi.ss.usermodel.{CellType, DateUtil}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import scala.jdk.CollectionConverters._

val fn = new File("/home/vm/wip/qpointz/flow/flow-excel/src/test/resources/flow-excel-test/TestData.xlsx")
val fs = new FileInputStream(fn)

val ws = new XSSFWorkbook(fs)

ws.getNumberOfSheets

val s = ws.getSheetAt(0)
val rowIter = s.rowIterator().asScala


val vectors = rowIter.map(r=> {
  val cellIter = r.cellIterator().asScala

  val values = cellIter.toSeq.map(c=> {
    if (c.getCellType== CellType.NUMERIC &&  DateUtil.isCellDateFormatted(c)) {
      "DATE"
    } else {
      c.getCellType.toString
    }
  })

  Vector(values)
})

println(vectors.toSeq)