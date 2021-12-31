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

import io.qpointz.flow.Record
import io.qpointz.flow.ql.types.QAny

trait QRecordFunction[+R] extends (Record => R)
sealed trait QRecordProjectionFunction extends QRecordFunction[Record]
sealed trait QRecordMatchFunction extends QRecordFunction[Boolean]

trait QlFunction[+R] {
  def apply(p:List[QAny[_]]):QAny[R]
}

trait QlFunction1[-T1, +R] extends ((QAny[T1]) => QAny[R]) with QlFunction[R] {
 def apply(t1: QAny[T1]):QAny[R]
}

trait QFunction2[-T1, -T2, +R] extends ((QAny[T1], QAny[T2]) => R)
  with QlFunction[R] {
}

