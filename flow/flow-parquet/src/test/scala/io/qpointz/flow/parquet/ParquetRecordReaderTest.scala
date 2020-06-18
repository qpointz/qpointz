package io.qpointz.flow.parquet

import org.apache.parquet.io.InputFile
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ParquetRecordReaderTest extends AnyFlatSpec with Matchers {

  behavior of "read"

  it should "read simple file" in  {
    val s  = new ParquetRecordReaderSettings()
    s.inputFile = InputFile
  }

}
