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
 *  limitations under the License
 */

package io.qpointz.flow.serialization

import io.qpointz.flow.serialization.extclasses._
import org.json4s.jackson.Serialization.{write, read}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.json4s.jackson.JsonMethods._

class JsonTest extends AnyFlatSpec with Matchers {

  behavior of "Json"

  it should "serialize/deserialize basics" in {
    implicit val formats = io.qpointz.flow.serialization.Json.formats
    val a = List[TestCase](
      new TestCaseC("a1","b1"),
      TestCaseA("a2","b2"),
      new TestCaseB("a3","b3"),
      new TestCaseB("a4","b4"),
    )
    val js = pretty(render(parse(write(a))))
    println(js)
    val aa = read[List[Any]](js).map(_.asInstanceOf[TestCase]).toArray
    val res = a
      .zipWithIndex
      .map(x=> (x._1, aa(x._2)))
      .map(x=> (x._1, x._2, x._1.getClass == x._2.getClass && x._1.a == x._2.a && x._1.b == x._2.b))
    res.forall(_._3==true) shouldBe true
  }

}
