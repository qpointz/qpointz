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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class QlFunctionNTest extends AnyFlatSpec with Matchers {

  def testcall[T<:QlFunction[R],R](f:T, fp:T=>R, expect:R, l:List[Any]):Unit = {
    f.apply(l) shouldBe expect
    fp(f) shouldBe expect

    //test more parameters passed in the args list
    val more = (1 to 50).map(_.toString).toList
    assertThrows[NoSuchElementException] {
      f.apply(more)
    }

    if (l.length>0) { //not applicable to Func0
      val less = l.take(l.length-1)
      assertThrows[NoSuchElementException] {
        f.apply(less)
      }
    }
  }

  behavior of "Funcs"

  it should "Function0" in {
    testcall(QlFunction("tfunc", ()=>"A"),
       (x:QlFunction0[String])=> x(),
      "A",
      List())
  }

  it should "Function1" in {
    testcall[QlFunction1[String,String],String](QlFunction("tfunc", (a:String)=>a),
      (x:QlFunction1[String,String]) => x("A"),
    "A",
      List("A"))
  }

  it should "Function2" in {
    testcall[QlFunction2[String, String,String],String](QlFunction("tfunc", (a:String, b:String)=>s"$a$b"),
      (x:QlFunction2[String,String,String]) => x("A","B"),
      "AB",
      List("A","B"))
  }

  it should "Function3" in {
    testcall[QlFunction3[String, String, String,String],String](QlFunction("tfunc", (a:String, b:String, c:String)=>s"$a$b$c"),
      (x:QlFunction3[String,String,String,String]) => x("A","B","C"),
      "ABC",
      List("A","B","C"))
  }

  it should "Function4" in {
    testcall[QlFunction4[String, String, String, String,String],String](QlFunction("tfunc", (a:String, b:String, c:String, d:String)=>s"$a$b$c$d"),
      (x:QlFunction4[String,String,String,String,String]) => x("A","B","C","D"),
      "ABCD",
      List("A","B","C","D"))
  }

  it should "Function5" in {
    testcall[QlFunction5[String, String, String, String, String,String],String](QlFunction("tfunc", (a:String, b:String, c:String, d:String, e:String)=>s"$a$b$c$d$e"),
      (x:QlFunction5[String,String,String,String,String,String]) => x("A","B","C","D","E"),
      "ABCDE",
      List("A","B","C","D","E"))
  }

  it should "Function6" in {
    testcall[QlFunction6[String, String, String, String, String, String,String],String](QlFunction("tfunc", (a:String, b:String, c:String, d:String, e:String, f:String)=>s"$a$b$c$d$e$f"),
      (x:QlFunction6[String,String,String,String,String,String, String]) => x("A","B","C","D","E","F"),
      "ABCDEF",
      List("A","B","C","D","E","F"))
  }

  it should "Function7" in {
    testcall[QlFunction7[String, String, String, String, String, String,String,String],String](QlFunction("tfunc", (a:String, b:String, c:String, d:String, e:String, f:String, g:String)=>s"$a$b$c$d$e$f$g"),
      (x:QlFunction7[String, String, String, String, String, String,String,String]) => x("A","B","C","D","E","F","G"),
      "ABCDEFG",
      List("A","B","C","D","E","F","G"))
  }

  it should "Function8" in {
    testcall[QlFunction8[String, String, String, String, String, String, String,String,String],String](QlFunction("tfunc", (a:String, b:String, c:String, d:String, e:String, f:String, g:String, h:String)=>s"$a$b$c$d$e$f$g$h"),
      (x:QlFunction8[String, String, String, String, String, String, String,String,String]) => x("A","B","C","D","E","F","G","H"),
      "ABCDEFGH",
      List("A","B","C","D","E","F","G","H"))
  }

  it should "Function9" in {
    testcall[QlFunction9[String, String, String, String, String, String, String, String,String,String],String](QlFunction("tfunc", (a:String, b:String, c:String, d:String, e:String, f:String, g:String, h:String, j:String)=>s"$a$b$c$d$e$f$g$h$j"),
      (x:QlFunction9[String, String, String, String, String, String, String, String,String,String]) => x("A","B","C","D","E","F","G","H","J"),
      "ABCDEFGHJ",
      List("A","B","C","D","E","F","G","H","J"))
  }

  it should "Function10" in {
    testcall[QlFunction10[String, String, String, String, String, String, String, String, String,String,String],String](QlFunction("tfunc", (a:String, b:String, c:String, d:String, e:String, f:String, g:String, h:String, j:String, k:String)=>s"$a$b$c$d$e$f$g$h$j$k"),
      (x:QlFunction10[String, String, String, String, String, String, String, String,String,String,String]) => x("A","B","C","D","E","F","G","H","J","K"),
      "ABCDEFGHJK",
      List("A","B","C","D","E","F","G","H","J","K"))
  }

}
