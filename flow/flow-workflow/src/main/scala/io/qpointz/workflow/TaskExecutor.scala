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

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.javadsl.Behaviors

import java.util.UUID

object TaskExecutor {

  sealed trait Command

  case class Init(replyTo: ActorRef[TaskStatus]) extends Command

  sealed trait TaskStatus

  case class Inited(uuid: UUID, from: ActorRef[Command]) extends TaskStatus

  def apply(taskInfo: TaskInfo): Behavior[Command] = Behaviors.setup { context =>
    val uuid: UUID = UUID.randomUUID()
    Behaviors.receiveMessage { msg =>
      msg match {
        case Init(rt) =>
          rt ! Inited(uuid, context.getSelf)
          Behaviors.same
      }
    }
  }
}
