/*
 * Copyright 2020 qpointz.io
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
 *
 */

package io.qpointz.flow.text

import java.io.File

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CsvRecordReaderTest extends AnyFlatSpec with Matchers {

  def defaultSettings = {
    val ss = new CsvRecordReaderSettings()
    ss.lineSeparator = Array('\n')
    ss.delimiter = ","
    ss.headerExtractionEnabled = true
    ss
  }

  def fileReader(name:String = "vanila", settings:CsvRecordReaderSettings = defaultSettings): CsvRecordReader = {
    val source: TextSource = TextSource(new File(s"flow/test/formats/csv/${name}.csv"))
    new CsvRecordReader(source, settings)
  }

  behavior of ("read")

  it should "return only content rows" in {
      fileReader().size shouldBe (10)
  }

  it should "return all attributes" in {
    val hr = fileReader().head
    hr.attributes.keys should contain allOf("id","first_name","last_name","email","gender","ip_address","date1","date2","date3","inscope","lon","lat")
  }

  it should "return all values" in {
    val em = Map(
      "id" 		    -> "1",
      "first_name"-> "Starlin",
      "last_name"	-> "Yoakley",
      "email"		  -> "syoakley0@wikia.com",
      "gender"	  -> "Female",
      "ip_address"-> "191.53.183.123",
      "date1" -> "2019/02/06",
      "date2" -> "21:51:08",
      "date3" -> "2019-02-17T21:36:30Z",
      "inscope" -> "true",
      "lon" -> "-35.4124355",
      "lat" -> "-9.0047392"
    )

    val hr = fileReader().head
    hr.attributes.toMap shouldEqual em
  }


//  behavior of "select by index"
//
//  it should "contains only selected columns" in {
//    val records = new CsvRecordReader(fileSrc("vanila"),testSettings).toList
//
//    val r = records.head
//
//    Seq(r("id"), r("last_name"), r("gender")) shouldBe Seq("1", "Yoakley", "Female")
//    Seq(r(0), r(1), r(2)) shouldBe Seq("1", "Yoakley", "Female")
//    r.size shouldBe 3
//  }
//
//  it should "renames indexed" in {
//    val r = new CsvRecordReader(fileSrc("vanila"), testSettings).toList.head
//
//    r.keySet should contain("id_renamed")
//    r("id_renamed") shouldBe "1"
//  }
//
//  it should "ignore missing indexes" in {
//    val r = new CsvRecordReader(fileSrc("vanila"), testSettings).toList.head
//
//    r.keySet should contain("id_renamed")
//    r("id_renamed") shouldBe "1"
//    r.size should be(1)
//  }
//
//  it should "use default names for attributes" in {
//    val r = new CsvRecordReader(fileSrc("vanila"), testSettings).toList.head
//
//    r.keySet should contain("__0")
//    r.keySet should contain("__1")
//  }
//
//  behavior of "select by name"
//
//  it should "contain only selected names" in {
//    val r = new CsvRecordReader(fileSrc("vanila"), testSettings).toList.head
//
//    r.keySet.size shouldBe 3
//    Seq( r("first_name"), r("last_name"), r("id")) shouldBe Seq("Starlin","Yoakley","1")
//  }
//
//  it should "ignore missing names" in {
//    val r = new CsvRecordReader(fileSrc("vanila"), testSettings).toList.head
//    r.keySet.size shouldBe 1
//    r(0) shouldBe "1"
//  }
//
//  it should "rename columns when provided" in {
//    val records = new CsvRecordReader(fileSrc("vanila"), testSettings).toList
//    val r = records.head
//    Seq( r("fn"), r("ln"), r(2)) shouldBe Seq("Starlin","Yoakley", "1")
//  }
//
//


}
