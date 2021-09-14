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

package io.qpointz.workflow.tasks

import scala.collection._

case class Transition[T,ST](from:T, when:ST, to:T) {}

trait TaskGraph[T, ST] {

  val states:Map[T,ST]

  val edges: Set[Transition[T,ST]]

  def isCompleted(state:Option[ST]):Boolean

  def whenf (state:ST, when:ST):Boolean

  def tasks: Set[T] = edges
    .map(x=> Set(x.from, x.to))
    .flatten
    .toSet

  def tasksByState(f:Option[ST]=>Boolean): Set[T] = tasks
    .map(x=>(x,states.get(x)))
    .filter(x=> f(x._2))
    .map(_._1)
    .toSet

  def notCompleted:Set[T] = tasksByState(x=> !isCompleted(x))

  def completed:Set[T] = tasksByState(x=> isCompleted(x))

  def roots: Set[T] = {
    val dependants = edges.map(_.to)
    tasks
      .filter(x=> !dependants.contains(x))
      .toSet
  }

  def hasRoots:Boolean = roots.nonEmpty

  def isValid : Boolean = hasRoots && cycles().isEmpty

  def cycles():Set[Seq[T]] = {
    def loop (t:T, path:Seq[T]): List[Seq[T]] = {
      if (path.contains(t)) {
        List(path :+ t)
      } else {
        edges.filter(_.from.equals(t)).toList match {
          case Nil => List()
          case x => x.map(z=>loop(z.to , path :+ z.from)).flatten
        }
      }
    }
    roots
      .map(loop(_, Seq()))
      .flatten
      .filter(x=> x.nonEmpty)
      .toSet
  }

  def next():Set[T] = {
    def readyDependants = edges
      .filter(x=> !isCompleted(states.get(x.to)))
      .map(x=> (states.get(x.from), x.when, x.to))
      .map(x=> x match {
        case (Some(s),w,t) => (t, whenf(s,w))
        case (None,_,t) => (t, false)
      })
      .groupBy(_._1)
      .filter(i=> i._2.map(_._2).fold(true)((a,b)=>a && b))
      .map(_._1)
      .toSet

    (completed, notCompleted) match {
      case (_, nc) if nc.isEmpty => Set()
      case (c, _) if c.isEmpty => roots
      case _ => readyDependants
    }
  }

}