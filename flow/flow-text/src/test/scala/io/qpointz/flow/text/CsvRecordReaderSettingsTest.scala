/*
 * Copyright 2021 qpointz.io
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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}

class CsvRecordReaderSettingsTest extends AnyFlatSpec with Matchers {

  behavior of "serialitation"

  implicit val formats:Formats = org.json4s.DefaultFormats

/*  it should "write" in {
    val s = CsvRecordReaderSettings(
      format = CsvFormat(
        delimiter = ",",
        lineSeparator = Array('b')
      ))

    val json = write(s)
    print(json)
    val s1 = read[CsvRecordReaderSettings](json)
    s shouldEqual s1
  }*/

}
