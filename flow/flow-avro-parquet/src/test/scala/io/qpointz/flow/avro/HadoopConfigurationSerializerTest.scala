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

import org.apache.hadoop.conf.Configuration
import org.json4s.{Extraction, StringInput}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HadoopConfigurationSerializerTest extends AnyFlatSpec with Matchers {

  behavior of "Configuration serializer"

  import io.qpointz.flow.serialization.Json._
  import org.json4s.jackson.JsonMethods._

  implicit val fmts = formats

  lazy val testCfg = {
    val cfg = new Configuration(true)
    cfg.set("myopt", "myoptvalue")
    cfg.set("triopt", "trioptvalue")
    cfg
  }

  it should "serialize" in {
    val vl = Extraction.decompose(testCfg)
    val nconfig = Extraction.extract[Configuration](vl)
    val nc = Extraction.decompose(nconfig)
    nconfig.get("myopt") shouldBe ("myoptvalue")
    vl shouldBe (nc)
  }

  it should "deserialize with defaults" in {
    val vl = Extraction.decompose(testCfg)
    val dconfig= Extraction.extract[Configuration](parse("""{"myopt":"myoptvalue", "triopt":"trioptvalue"}"""))
    dconfig.get("myopt") shouldBe("myoptvalue")
  }

}
