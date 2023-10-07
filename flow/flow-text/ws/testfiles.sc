import java.io.{File, FileInputStream, FileOutputStream}

import com.univocity.parsers.csv.CsvParserSettings
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.fixed.{FixedWidthFields, FixedWidthWriter, FixedWidthWriterSettings}
import com.univocity.parsers.tsv._
import scala.collection.JavaConverters._


val colSep = ","
val lineSep = "\n"

val ps = new CsvParserSettings()
ps.setLineSeparatorDetectionEnabled(false)
ps.setHeaderExtractionEnabled(true)

val fmt = ps.getFormat
fmt.setLineSeparator(lineSep)
fmt.setDelimiter(colSep)


val p = new CsvParser(ps)

val fis = new FileInputStream(new File("./flow/test/formats/csv/good.csv").getAbsoluteFile)
val records = p.parseAllRecords(fis)

val ml= records.iterator().asScala.map(x=>{
  x.getValues.toIterator.zipWithIndex.toList
}).flatten.toList
  .map(u=> (u._2, u._1))
  .groupBy(_._1)
  .map(u=> (u._1, u._2.map(_._2.length()).max))
  .toList
  .sortBy(_._1)
  .map(_._2+2)
  .toArray

val headers = records.iterator().asScala.map(x=>{
  x.getMetaData.headers().zipWithIndex.toList
}).flatten.toList
  .distinct
  .sortBy(_._2)
  .map(_._1)
  .distinct
  .toArray


val fixflds = new FixedWidthFields(headers, ml)

val fwfs = new FixedWidthWriterSettings(fixflds)
val fwf = fwfs.getFormat
val outfwf = new FileOutputStream(new File("./flow/test/formats/fwf/good.csv").getAbsoluteFile)
val fwfw  = new FixedWidthWriter(outfwf, fwfs)

records.forEach(b=>fwfw.writeRow(b.getValues()))
fwfw.close()

val tsvset = new TsvWriterSettings()
tsvset.setHeaders(headers:_*)
val outtsv = new FileOutputStream(new File("./flow/test/formats/tsv/good.csv").getAbsoluteFile)
val tsvw = new TsvWriter(outtsv, tsvset)

records.forEach(b=>tsvw.writeRow(b.getValues()))
tsvw.close()