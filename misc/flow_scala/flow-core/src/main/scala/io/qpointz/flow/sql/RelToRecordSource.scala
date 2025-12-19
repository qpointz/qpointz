/*
 *
 *  Copyright 2022 qpointz.io
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.qpointz.flow.sql

import io.qpointz.flow.RecordReader
import org.apache.calcite.rel.RelNode
import org.apache.calcite.rel.logical.{LogicalProject, LogicalTableScan}

import scala.jdk.CollectionConverters._


object RelToRecord {

  def apply(rel:RelNode, inputs:Map[List[String], RecordReader]):RecordReader = {
    rel match {
      case lp:LogicalTableScan => inputs(lp.getTable.getQualifiedName.asScala.toList)
      case unk:RelNode => throw new RuntimeException(unk.toString)
    }
  }

}
