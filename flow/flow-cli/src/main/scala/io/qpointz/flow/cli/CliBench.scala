/*
 * Copyright 2021 qpointz.io
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.qpointz.flow.cli

import io.qpointz.flow.MetadataMethods._
import io.qpointz.flow.Record
import io.qpointz.flow.avro.{AvroRecordWriter, AvroRecordWriterSettings, ConstantAvroScemaSource}
import io.qpointz.flow.nio.FileStreamSource
import io.qpointz.flow.text.csv.{CsvFormat, CsvRecordReader, CsvRecordReaderSettings}
import io.qpointz.flow.transformations.{RecordTransformation, TransformationRecordReader}
import org.apache.avro.{LogicalTypes, Schema, SchemaBuilder}

import java.io.File
import java.nio.file.Path

object CliBench {

  def main(args:Array[String]) = {

    import scala.jdk.CollectionConverters._
    val alll = ClassLoader.getSystemResources("META-INF/io.qpointz/1.conf").asScala.toList

    val csv = CsvRecordReaderSettings()
      .format(CsvFormat()
        .lineSeparator("\n")
        .delimiter (",")
      )
      .headerExtractionEnabled (true)

    val source = FileStreamSource(new File(s"flow/test/avro/large.csv"))
    val reader = new CsvRecordReader(source, csv)

    val trans = new RecordTransformation {
      override def transform(r: Record): Record = {
        r.put(Map(
          "id" -> {r.getOp("id") match {
            case  Some(x:String) => x.toInt
            case Some(x) => -1
            case None => -2
          }},
          "inscope" -> {r.getOp("inscope") match {
            case Some(x:String) => x.toBoolean
            case _ => null
          }}
        ), empty)
      }
    }

    val rreader = new TransformationRecordReader(trans, reader)

    val miliType = LogicalTypes.date().addToSchema(Schema.create(Schema.Type.INT))


    val avro = new AvroRecordWriterSettings()
    avro.path = Path.of(s"flow/tmp/out.avro")
    avro.schema = new ConstantAvroScemaSource(SchemaBuilder
      .record("default")
      .fields()
        .name("id").`type`(miliType).noDefault()
        .nullableString("first_name","NULL")
        .nullableString("last_name","NULL")
        .nullableString("email","NULL")
        .nullableString("gender","NULL")
        .nullableString("ip_address","NULL")
        .nullableString("date1","NULL")
        .nullableString("date2","NULL")
        .nullableString("date3","NULL")
        .requiredBoolean("inscope")
        .nullableString("lon","NULL")
        .nullableString("lat","NULL")
      .endRecord()
    )

    val writer = new AvroRecordWriter(avro)
    writer.open()
    rreader.foreach(writer.write(_))
    writer.close()
  }



}
