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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.InputStream
import scala.jdk.CollectionConverters._

class AvroSchemaSourceTest extends AnyFlatSpec with Matchers {

  def gs(name:String):InputStream = {
    val p = s"flow-avro-parquet-test/schemas/$name.json"
    this.getClass.getClassLoader.getResourceAsStream(p)
  }

  behavior of "JsonAvroSchema"

  it should "read" in  {
    val s = AvroSchemaSource(gs("simple")).avroSchema()
    s.getFields.asScala.length shouldBe 3
  }


}
