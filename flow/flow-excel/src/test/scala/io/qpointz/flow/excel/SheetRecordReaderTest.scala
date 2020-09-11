/*
 * Copyright 2019 qpointz.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.qpointz.flow.excel

import java.time.ZonedDateTime._
import java.time.{ZoneId, ZonedDateTime}
import java.util.Date

import io.qpointz.flow.AttributeValue
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SheetRecordReaderTest extends AnyFlatSpec with Matchers {

  import ExcelTestMethods._

  val testColumns = List(
    SheetColumn(0, "id"),
    SheetColumn(1, "first_name"),
    SheetColumn(2, "last_name"),
    SheetColumn(3, "email"),
    SheetColumn(4, "gender"),
    SheetColumn(5, "birthday"),
    SheetColumn(6, "registration"),
    SheetColumn(7, "orders"),
    SheetColumn(8, "sum"),
    SheetColumn(9, "rate")
  )

  val testSheet = sheet("TestData","TestData")

  val testSettings = new SheetRecordReaderSettings()
  testSettings.recordLabel = "TESTDATA"
  testSettings.columns = testColumns

  behavior of "iterator"

  it should "return records" in {
    val td = new SheetRecordReader(testSheet, testSettings, List())
    val recs = td.toSeq
    recs.length should be > 0
  }

  val typest = new SheetRecordReaderSettings()
  typest.recordLabel = "TYPES"
  typest.columns = List(
    SheetColumn(0, "Id"),
    SheetColumn(1, "Type"),
    SheetColumn(2, "Value")
  )
  val typesheet = ExcelTestMethods.sheet("TypesValues", "In")
  val typerd = new SheetRecordReader(typesheet, typest, List())
  val records = typerd.toList

  def recById(id:Int) = records.find(_.get("Id")==id).get
  def valueById(id:Int) = recById(id).get("Value")

  /*"records" should "contain unexpected value" in {
    val tags = recById(11).metadata()
    assert(false)
  }*/

  "cell value" should "return records" in {
    records.length should be > 0
  }

  it should "return date" in {
    val zdt = ZonedDateTime.of(2007, 12, 12, 14, 30, 0, 0, ZoneId.systemDefault())
    val dt = Date.from(zdt.toInstant)
    val exd = valueById(1)
    exd should be(dt)
  }

  it should "return double" in {
    valueById(2) should be (12.34)
  }

  it should "return integer" in {
    valueById(3) should be(4)
  }

  it should "return boolean(true)" in {
    valueById(4) shouldBe(true)
  }

  it should "return boolean(false)" in {
    valueById(5) shouldBe(false)
  }

  it should "return strings" in {
    valueById(6) should be("Foo Bar")
  }

  it should "return formulas value" in {
    valueById(7) should be(3)
  }

  it should "return blank" in {
    valueById(8) should be(AttributeValue.Empty)
  }

  it should "return nulls" in {
    valueById(9) should be(AttributeValue.Missing)
  }

  it should "return errors" in {
    valueById(10) should be(AttributeValue.Error)
  }

}
