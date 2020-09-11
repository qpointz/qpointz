import java.io.{File, FileInputStream}

import com.univocity.parsers.csv.CsvParserSettings
import com.univocity.parsers.csv.CsvParser

val colSep = ","
val lineSep = "\n"

val ps = new CsvParserSettings()
ps.setLineSeparatorDetectionEnabled(false)
ps.setHeaderExtractionEnabled(true)

val fmt = ps.getFormat
fmt.setLineSeparator(lineSep)
fmt.setDelimiter(colSep)


val p = new CsvParser(ps)

val fis = new FileInputStream(new File("test/data/formats/csv/good.csv").getAbsolutePath)
val records = p.parseAllRecords(fis)

records.size()

records.get(0).getString(0)
records.get(4).getString(2)
records.get(4).getString(4)

records.get(0).getValue(22,None)
records.get(0).getString("dklfskdlfkdslfk")

