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

package io.qpointz.flow.transform.simple

import java.time.Instant

import io.qpointz.flow.{AuditMessage, InMemoryOperationContext, OperationContext, Record, RecordReader, RecordWriter}
import org.scalatest.matchers.should.Matchers
import org.scalamock.scalatest.MockFactory

class WriteRecordsTest extends org.scalatest.flatspec.AnyFlatSpec with Matchers with MockFactory {


  final class MockRecordReader(private val records:Iterable[Record])(implicit val ctx:OperationContext) extends RecordReader {
    case class BeginRead(s:String, t:Instant = Instant.now()) extends AuditMessage
    override def iterator: Iterator[Record] = {
      ctx.audit.log(BeginRead("begin read"))
      records.iterator
    }
  }

  implicit val ctx = InMemoryOperationContext

  behavior of "WriteRecord"


  it should "open and close writer and write records in sequence" in {
    //val records = List[Record]().iterator
    val recs = List(
      Record("a"->1, "b"->"foo"),
      Record("a"->2, "b"->"bar"),
    )

    val reader = new MockRecordReader(recs)

    val writer = mock[RecordWriter]

    val settings = new WriteRecordsSettings()
    settings.openWriter(true)
    settings.closeWriter(true)

    val rw = new WriteRecords(reader, writer, settings)

    (writer.open _).expects()
    (writer.write _).expects(recs(0)).repeat(1)
    (writer.write _).expects(recs(1)).repeat(1)
    (writer.close _).expects()

    rw.transform()
  }

}
