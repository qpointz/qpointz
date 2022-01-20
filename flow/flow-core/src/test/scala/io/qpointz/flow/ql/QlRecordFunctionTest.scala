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

package io.qpointz.flow.ql

import io.qpointz.flow.Record
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class QlRecordFunctionTestextends extends AnyFlatSpec with Matchers {

  val defaultRec = Record("F1" -> 1, "F2" -> 2, "F3" -> 3, "F4" -> 4, "F5" -> 5)

  def testcall[T <: QlRecordFunction[R], R](f: T, fp: T => R, expect: R, l: List[Any]): Unit = {
    f.apply(l) shouldBe expect
    fp(f) shouldBe expect


    //test apply
    f.apply(defaultRec, l.tail) shouldBe expect

    //test more parameters passed in the args list
    val more = (1 to 50).map(_.toString).toList
    assertThrows[NoSuchElementException] {
      f.apply(more)
    }


    if (l.length > 0) { //not applicable to Func0
      val less = l.take(l.length - 1)
      assertThrows[NoSuchElementException] {
        f.apply(less)
      }
    }
  }

  behavior of "RecordFuncs"

  it should "Function0" in {
    testcall(QlRecordFunction(r => s"${r.items.length}A"),
      (x: QlRecordFunction0[String]) => x(defaultRec),
      "5A",
      List(defaultRec))
  }


  it should "Function1" in {
    testcall[QlRecordFunction1[String, String], String](QlRecordFunction((r: Record, a: String) => s"${r.items.length}$a"),
      (x: QlRecordFunction1[String, String]) => x(defaultRec, "A"),
      "5A",
      List(defaultRec, "A"))
  }


  it should "Function2" in {
    testcall[QlRecordFunction2[String, String, String], String](QlRecordFunction((r: Record, a: String, b: String) => s"${r.items.length}$a$b"),
      (x: QlRecordFunction2[String, String, String]) => x(defaultRec, "A", "B"),
      "5AB",
      List(defaultRec, "A", "B"))
  }

  it should "Function3" in {
    testcall[QlRecordFunction3[String, String, String, String], String](QlRecordFunction((r: Record, a: String, b: String, c: String) => s"${r.items.length}$a$b$c"),
      (x: QlRecordFunction3[String, String, String, String]) => x(defaultRec, "A", "B", "C"),
      "5ABC",
      List(defaultRec, "A", "B", "C"))
  }


  it should "Function4" in {
    testcall[QlRecordFunction4[String, String, String, String, String], String](QlRecordFunction((r: Record, a: String, b: String, c: String, d: String) => s"${r.items.length}$a$b$c$d"),
      (x: QlRecordFunction4[String, String, String, String, String]) => x(defaultRec, "A", "B", "C", "D"),
      "5ABCD",
      List(defaultRec, "A", "B", "C", "D"))
  }


  it should "Function5" in {
    testcall[QlRecordFunction5[String, String, String, String, String, String], String](QlRecordFunction((r: Record, a: String, b: String, c: String, d: String, e: String) => s"${r.items.length}$a$b$c$d$e"),
      (x: QlRecordFunction5[String, String, String, String, String, String]) => x(defaultRec, "A", "B", "C", "D", "E"),
      "5ABCDE",
      List(defaultRec, "A", "B", "C", "D", "E"))
  }

  it should "Function6" in {
    testcall[QlRecordFunction6[String, String, String, String, String, String, String], String](QlRecordFunction((r: Record, a: String, b: String, c: String, d: String, e: String, f: String) => s"${r.items.length}$a$b$c$d$e$f"),
      (x: QlRecordFunction6[String, String, String, String, String, String, String]) => x(defaultRec, "A", "B", "C", "D", "E", "F"),
      "5ABCDEF",
      List(defaultRec, "A", "B", "C", "D", "E", "F"))
  }

  it should "Function7" in {
    testcall[QlRecordFunction7[String, String, String, String, String, String, String, String], String](QlRecordFunction((r: Record, a: String, b: String, c: String, d: String, e: String, f: String, g: String) => s"${r.items.length}$a$b$c$d$e$f$g"),
      (x: QlRecordFunction7[String, String, String, String, String, String, String, String]) => x(defaultRec, "A", "B", "C", "D", "E", "F", "G"),
      "5ABCDEFG",
      List(defaultRec, "A", "B", "C", "D", "E", "F", "G"))
  }

  it should "Function8" in {
    testcall[QlRecordFunction8[String, String, String, String, String, String, String, String, String], String](QlRecordFunction((r: Record, a: String, b: String, c: String, d: String, e: String, f: String, g: String, h: String) => s"${r.items.length}$a$b$c$d$e$f$g$h"),
      (x: QlRecordFunction8[String, String, String, String, String, String, String, String, String]) => x(defaultRec, "A", "B", "C", "D", "E", "F", "G", "H"),
      "5ABCDEFGH",
      List(defaultRec, "A", "B", "C", "D", "E", "F", "G", "H"))
  }

  it should "Function9" in {
    testcall[QlRecordFunction9[String, String, String, String, String, String, String, String, String, String], String](QlRecordFunction((r: Record, a: String, b: String, c: String, d: String, e: String, f: String, g: String, h: String, j: String) => s"${r.items.length}$a$b$c$d$e$f$g$h$j"),
      (x: QlRecordFunction9[String, String, String, String, String, String, String, String, String, String]) => x(defaultRec, "A", "B", "C", "D", "E", "F", "G", "H", "J"),
      "5ABCDEFGHJ",
      List(defaultRec, "A", "B", "C", "D", "E", "F", "G", "H", "J"))
  }
}