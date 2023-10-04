/*
 *  Copyright 2021 qpointz.io
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package io.qpointz.flow.avro

import io.qpointz.flow.Record
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericRecord, GenericRecordBuilder}

object AvroMethods {

  implicit class RecordMethods(record:Record) {

    def toGenericRecord(schema: Schema):GenericRecord = {
      val grb = new GenericRecordBuilder(schema)
       record
        .attributes
        .foldLeft(grb)((x,k)=> x.set(k._1,k._2))
        .build()
    }
  }


}
