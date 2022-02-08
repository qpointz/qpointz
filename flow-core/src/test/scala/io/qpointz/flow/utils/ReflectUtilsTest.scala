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

package io.qpointz.flow.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


trait TCla {
  def hallo:Int
}

class TestClass2(i:Int) extends TCla {
  override def hallo: Int = 11
}

class TestClass extends TCla {
  val a = 1
  var b = 2

  override def hallo: Int = 22
}

class ReflectUtilsTest extends AnyFlatSpec with Matchers {

  behavior of "tryNewInstanceByName"

  it should "return instance" in {
    val i = reflect.tryNewInstanceByName[TestClass]("io.qpointz.flow.utils.TestClass")
    i.isSuccess shouldBe true
  }

  it should "fail if not exists" in {
    val i = reflect.tryNewInstanceByName[TestClass]("io.qpointz.flow.TestClassMissing")
    i.isSuccess shouldBe false
  }

  it should "fail if constructor missing" in {
    val i = reflect.tryNewInstanceByName[TestClass]("io.qpointz.flow.utils.TestClass2")
    i.isSuccess shouldBe false
  }

  behavior of "instancesOf"

  sealed trait A {}
  sealed trait B {}

  case class AC(n:Int) extends A
  case class BC(n:Int) extends B
  case class ABC(n:Int) extends A with B
  case class C(n:Int)

  val allt = Seq(
    AC(1),
    BC(1),
    ABC(1),
    C(1),
  )

  it should "return implementers" in {
    val a:Seq[A] = reflect.instancesOf[A](allt)
    a.length shouldBe 2
    a should contain allElementsOf(Seq(AC(1), ABC(1)))
  }

  it should "return classes" in {
    val a:Seq[C] = reflect.instancesOf[C](allt)
    a.length shouldBe 1
    a.head shouldBe C(1)
  }

}
