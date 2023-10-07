/*
 *  Copyright 2021 qpointz.io
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package io.qpointz.flow.avro

import org.apache.avro.Schema
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._

import java.io.InputStream

trait AvroSchemaSource {
  def avroSchema():Schema
}

object AvroSchemaSource {
  def apply(schema: Schema): AvroSchemaSource = new ConstantAvroScemaSource(schema)

  def apply(in: InputStream): AvroSchemaSource = {
    val cnt = scala.io.Source.fromInputStream(in).mkString
    new JsonAvroSchemaSource(cnt)
  }

  def apply(in:String):AvroSchemaSource = new JsonAvroSchemaSource(in)
}

final class ConstantAvroScemaSource(private val schema:Schema) extends AvroSchemaSource {
  override def avroSchema(): Schema = schema
}

final class JsonAvroSchemaSource(private val jsonStream:String) extends AvroSchemaSource {
  lazy val parsedSchema = {
    val parser = new org.apache.avro.Schema.Parser()
    parser.parse(jsonStream)
  }
  override def avroSchema(): Schema = parsedSchema
}

final class AvroSchemaSourceSerializer extends CustomSerializer[AvroSchemaSource] (implicit format => (
  {
    case jv:JValue => AvroSchemaSource(compact(render(jv)))

  },
  {case avss: AvroSchemaSource =>
    parse(avss.avroSchema().toString(true))
  }
))