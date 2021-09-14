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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.Set

class TaskGraphTest extends AnyFlatSpec with Matchers {

  object Graphs {

    def graph(s: Map[String, Boolean], t:Set[Transition[String,Boolean]]):TaskGraph[String,Boolean] = new TaskGraph[String, Boolean] {
      override val states: collection.Map[String, Boolean] = s
      override val edges: Set[Transition[String, Boolean]] = t

      override def isCompleted(state: Option[Boolean]): Boolean = state match {
        case None => false
        case Some(s) => s
      }

      override def whenf(state: Boolean, when: Boolean): Boolean = state == when
    }

    def graph(transitions: Transition[String, Boolean]*): TaskGraph[String, Boolean] = graph(Map[String,Boolean](), transitions.toSet)
    def graph(states: Map[String, Boolean], transitions: Transition[String, Boolean]*): TaskGraph[String, Boolean] = graph(states, transitions.toSet)
    def ifTrue(from:String, to:String):Transition[String,Boolean] = Transition(from, true, to)
    def ifFalse(from:String, to:String):Transition[String,Boolean] = Transition(from, false, to)


  }

  import Graphs._

  behavior of "sequential-next"

  it should "return first item" in {
    val g: TaskGraph[String, Boolean] = graph(
      ifTrue("a","b")
    )
    g.next() shouldBe Set("a")
  }

  it should "return next if some completed" in {
    val g: TaskGraph[String, Boolean] = graph(Map("a"->true),
      ifTrue("a", "b"),
      ifTrue("b", "c"),
      ifTrue("c", "d"))
    g.next() shouldBe Set("b")
  }

  it should "return empty id all completed" in {
    val g = graph(Map("a"->true, "b"->true, "c"->true, "d"->true),
      ifTrue("a", "b"),
      ifTrue("b", "c"),
      ifTrue("c", "d"))
    g.next() shouldBe Set()
  }

  behavior of "tree-next"

  it should "return all parallel" in {
    val g = graph(Map("a"->true),
      ifTrue("a", "b1"),
      ifTrue("a", "b2"),
      ifTrue("b1", "c"),
      ifTrue("b2", "c")
    )

    g.next() shouldBe Set("b1","b2")
  }

  behavior of "cycles"

  it should "return non self cycle paths" in {
    //        /<- \
    //  a -> b -> c -> d -> e - \ -> \
    //        \<- - - /      \<-/    |
    //        \<- - - - - - - - - - -/
    val g = graph(
      ifTrue("a","b"),
      ifTrue("b","c"),
      ifTrue("c","d"),
      ifTrue("d","b"),
      ifTrue("c","b"),
      ifTrue("d","e"),
      ifTrue("e","e"),
      ifTrue("e","b"),
    )
    g.cycles() shouldBe Set(
      Seq("a","b","c","b"),
      Seq("a","b","c","d","b"),
      Seq("a","b","c","d","e","e"),
      Seq("a","b","c","d","e","b"),
    )
  }

  behavior of "validation"

  it should "no roots" in {
    val g = graph(
      ifTrue("a", "b"),
      ifTrue("b", "c"),
      ifTrue("c", "a")
    )
    g.hasRoots shouldBe false
    g.isValid shouldBe false
  }
  
}