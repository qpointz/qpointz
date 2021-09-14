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
 *  limitations under the License.
 */

package io.qpointz.workflow

import com.typesafe.scalalogging.Logger
import io.qpointz.flow.ProgressContext

sealed trait TaskInfo

trait TaskContext {
  val log:Logger
  val progress: ProgressContext
}

sealed trait TaskStatus

trait TaskContextAware {
  val ctx : TaskContext
}

case class RunStatus() extends TaskStatus
case class InitStatus() extends TaskStatus
trait Task extends TaskContextAware  {
  def init:InitStatus
  def run:RunStatus
}

case class ValidationStatus() extends TaskStatus
trait ValidateTask extends TaskContextAware {
  def validate:ValidationStatus
}

case class DisposeStatus() extends TaskStatus
trait DisposeTask extends TaskContextAware {
  def dispose:DisposeStatus
}






