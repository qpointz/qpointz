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

package io.qpointz.flow

package object excel {

  case class SheetColumn(index:Int, header:String) {}

  type SheetColumnCollection = Seq[SheetColumn]

  implicit class SheetColumnCollectionOps(sc:SheetColumnCollection) {
    def byColIndexOp(idx:Int): Option[SheetColumn] = {
      sc.find(_.index==idx)
    }

    def colIndexMap(): Map[Int, SheetColumn] = { sc
      .map(k=>(k.index, k))
      .toMap
    }

  }

  class FlowExcelException(private val message: String = "",
                      private val cause: Throwable = None.orNull
                     ) extends FlowException(message, cause)

  type SheetSelectionSettingsCollection = List[SheetSelectionSettings]
}
