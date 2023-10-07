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

package io.qpointz.flow.ql.functions

import io.qpointz.flow.Record
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.MissingFormatArgumentException
import scala.util.{Failure, Success}

class StringFunctionsTest extends AnyFlatSpec with Matchers with FunctionTests {

  behavior of "STR"
  import StringFunctions._

  it should "conv by direct" in {
    str(1) shouldBe "1"
  }

  it should "conv by paramlist" in {
    str.apply(Seq(1)) shouldBe "1"
  }

  it should "conv in expression" in {
    sql("SELECT STR(A) AS A", Record("A"->1))(Record("A"->Success("1")))
  }

  behavior of "CONCAT"

  it should "concat strings" in {
    concat.apply(Seq("A","B","C")) shouldBe "ABC"
  }

  it should "concat in exp" in {
    sql("SELECT CONCAT(A1,B1,C1) AS CT", Record("A1"->"A", "B1"->"B", "C1"->"C"))(Record("CT"->Success("ABC")))
  }


  behavior of "FORMAT"

  it should "format" in {
    format(Seq("Hello %s, %s times", "Bob", 5)) shouldBe "Hello Bob, 5 times"
  }

  it should "not fail on zero args" in {
    format(Seq("Bob")) shouldBe "Bob"
  }

  it should "be callable directly" in {
    format("Hello %s %s", Seq("A","B")) shouldBe "Hello A B"
  }

  it should "fail on zero args" in {
    assertThrows[MissingFormatArgumentException] {
      format(Seq("Bob %s"))
    }
  }


  behavior of "REPLACE"

  it should "replace" in {
    replace(Seq("Hello Bob 2 times", "Bob", "Mark")) shouldBe "Hello Mark 2 times"
  }

  it should "replace in exp" in {
    sql("SELECT REPLACE(A, 'Bob', 'Mark') AS AN", Record("A"->"Hello Bob 2 times"))(Record("AN"->Success("Hello Mark 2 times")))
  }


  behavior of "SUBSTR"


  it should "substr begin and end" in {
    substr(Seq("ABCD", 1, 3)) shouldBe "BC"
  }


  it should "substr begin and end in exp" in {
    sql("SELECT SUBSTR(A, 1, 3) AS A", Record("A"->"ABCD"))(Record("A"->Success("BC")))
  }

  it should "substr begin" in {
    substr(Seq("ABCD", 2)) shouldBe "CD"
  }

  it should "substr begin in exp" in {
    sql("SELECT SUBSTR(A, 2) AS A", Record("A"->"ABCD"))(Record("A"->Success("CD")))
  }

  behavior of "REGEX_MATCHES"

  it should "match" in {
    regexMatches(Seq(raw"\d{5}","12345")) shouldBe true
  }

  it should "match expression" in {
    sql(raw"""SELECT IN_T, IS_MATCHES_RX('\d{3}', IN_T) AS IN_M""", Record("IN_T"->"123"))(Record("IN_T"->"123", "IN_M"->Success(true)))
  }

}
