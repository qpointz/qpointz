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

package io.qpointz.flow

import QId._

object QIds {

  val qp = NamespaceId("flow")

  object Stream {
    val hierarchyId = qp.hierarchyId("stream")

    val inputStreamId = hierarchyId.groupId("input")
    val outputStreamId = hierarchyId.groupId("output")

  }

  object Record {
    val hierarchyId = qp.hierarchyId("record")

    object Reader {
      val hierarchyId = QIds.Record.hierarchyId.hierarchyId("read")
      val reader = hierarchyId.groupId("reader")
      val settings = hierarchyId.groupId("settings")
    }

    object Writer {
      val hierarchyId = QIds.Record.hierarchyId.hierarchyId("write")
      val writer = hierarchyId.groupId("writer")
      val settings = hierarchyId.groupId("settings")
    }

    object Transformation {
      val hierarchyId = QIds.Record.hierarchyId.groupId("transformation")
    }
  }

  //qp:flow:record/reader:reader:csv
  //qp:flow:record/writer:settings:csv

}

