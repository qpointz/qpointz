import java.io.File

import com.univocity.parsers.common.{Context, DataProcessingException, ProcessorErrorHandler}

import scala.collection.JavaConverters._
import com.univocity.parsers.csv.{CsvParser, CsvParserSettings}

val f = new File("./libs/formats-text/src/ws/error.csv")

val ss = new CsvParserSettings()
val fmt = ss.getFormat()
fmt.setQuote('"')
fmt.setLineSeparator("\n")
fmt.setDelimiter(",")

ss.setDelimiterDetectionEnabled(false)
ss.setQuoteDetectionEnabled(false)
ss.setAutoConfigurationEnabled(false)
ss.setNormalizeLineEndingsWithinQuotes(true)

ss.setProcessorErrorHandler(new ProcessorErrorHandler[Context] {

  override def handleError(error: DataProcessingException,
                           inputRow: Array[AnyRef],
                           context: Context): Unit = {
    println("error")
  }
})
val p = new CsvParser(ss)
val iter = p.iterateRecords(f).iterator()

while (iter.hasNext) {
  var r = iter.next()
  var ctx = iter.getContext()
  println(s"${ctx.currentLine()}: ${r.getValues().toSeq}")
  println(r.getMetaData().headers().mkString(","))
}


  //.asScala.zipWithIndex.foreach(x=>println(s"${x._2}:${x._1.getValues.length}:${x._1}"))