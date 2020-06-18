package io.qpointz.flow.parquet

import java.nio.file.{Files, Paths}

import io.qpointz.flow.Record
import org.apache.avro.SchemaBuilder
import org.apache.hadoop.fs.Path

object ParquetUtils {

  def writeTestFile(fp:String) = {
    val filePath = Paths.get(fp)
    if (Files.exists(filePath)) {
      Files.delete(filePath)
    }

    val as = new ConstantAvroScemaSource(SchemaBuilder
      .record("default")
      .fields()
      .requiredString("a")
      .requiredString("b")
      .requiredString("c")

      .endRecord()
    )
    val s = new AvroParquetRecordWriterSettings()
    s.path = new Path(filePath.toAbsolutePath.toString)
    s.schema = as

    val w = new AvroParquetRecordWriter(s)
    w.open()

    for (i<- 1 to 1000) {
      w.write(Record("a" -> "a1", "b" -> "b1", "c" -> "c1"))
    }

    w.close()

  }


}
