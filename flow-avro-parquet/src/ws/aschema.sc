import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.avro.AvroParquetReader
import org.apache.parquet.hadoop.util.HadoopInputFile

val cfg = new Configuration()
val inputFile = HadoopInputFile.fromPath(new Path(".test/parquet-writer/apw.parquet"), cfg)
val ar = AvroParquetReader.builder[GenericRecord](inputFile)
  .build()

val gr = ar.read()

println(gr.get(0))
