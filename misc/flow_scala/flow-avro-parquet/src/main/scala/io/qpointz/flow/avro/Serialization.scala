/*
 * Copyright 2022 qpointz.io
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
 *  limitations under the License
 */

package io.qpointz.flow.avro

import io.qpointz.flow.{TypeId, flowQuids, serialization}
import io.qpointz.flow.serialization.{JsonProtocol, JsonProtocolExtension}
import org.apache.hadoop.conf.Configuration
import org.json4s.JsonAST.{JBool, JField}
import org.json4s.{CustomSerializer, JObject, JString}
import shaded.parquet.org.apache.thrift.protocol.TJSONProtocol

import scala.jdk.CollectionConverters._

class JsonSerialization extends JsonProtocolExtension {
  override def protocols: Iterable[serialization.JsonProtocol[_]] = List(
    JsonProtocol[AvroSchemaSource](new AvroSchemaSourceSerializer()),
    JsonProtocol[AvroRecordWriter](flowQuids.writer("avro"), new AvroRecordWriterSerializer()),
    JsonProtocol[AvroRecordWriterSettings](new AvroRecordWriterSettingsSerializer()),
    JsonProtocol[AvroRecordReader](flowQuids.reader("avro"), new AvroRecordReaderSerializer()),
    JsonProtocol[Configuration](new HadoopConfigurationSerializer)
  )
}

class HadoopConfigurationSerializer extends CustomSerializer[Configuration](implicit fmt=> (
  {
    case jo:JObject => {
      val config = new Configuration(true)
      val vals = jo.extractOrElse[Map[String,String]](Map[String,String]())
      vals.foreach(kv=>config.set(kv._1,kv._2))
      config
    }
  },
  {
    case cfg:Configuration => {
      val fields = cfg.iterator().asScala.map(kv=> JField(kv.getKey, JString(kv.getValue))).toList
      val res = JObject(fields)
      println(res)
      res
    }
  }
))