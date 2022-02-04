/*
 *
 *  Copyright 2022 qpointz.io
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.qpointz.flow.avro

import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Paths

class AvroRecordReaderTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  def avr(name:String="nyc-bronx-2009"):File = Paths.get("flow-test/data/formats/avro/nyc/", s"${name}.avro").toFile

  behavior of "read"

  it should "read" in {
    val r = new AvroRecordReader(avr())
    //r.take(10).toSeq.length shouldBe 10
    r.take(10).foreach(println)



  }

}
