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

trait TaskPlanner[T] {
  def next(completed:Set[T]):TaskSet
}

sealed trait TaskSet { }
object TaskSet {
  object Empty extends TaskSet
  case class Single[T](single: T) extends TaskSet
  case class Parallel[T](set: Set[T]) extends TaskSet
}

class SequentialPlanner[T](private val tasks:Seq[T]) extends TaskPlanner[T] {

  def next(completed:Set[T]):TaskSet= (tasks, completed.toSeq) match {
    case (Seq(), Seq()) => TaskSet.Empty
    case (all, Seq()) => TaskSet.Single(all.head)
    case _ =>
      val executed = tasks
        .zipWithIndex
        .map(x=> (x._2,x._1, completed.contains(x._1)))
      val minNotExecuted = executed.filter(!_._3).map(_._1).min
      val maxExecuted = executed.filter(_._3).map(_._1).max
      if (minNotExecuted > maxExecuted) {
        TaskSet.Single(tasks(minNotExecuted))
      } else {
        throw new IllegalArgumentException("List of executed task is not sequential")
      }
  }

}