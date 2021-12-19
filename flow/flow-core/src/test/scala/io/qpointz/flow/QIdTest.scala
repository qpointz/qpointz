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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.net.URI

class QIdTest  extends AnyFlatSpec with Matchers {

  import QId._

  behavior of "fromString"

  def expect(in:String, is:QId => Boolean, expect:QId):Unit = {
    val id = fromString(in)
    is(id) shouldBe true
    id shouldBe expect
    //id.toURI shouldBe URI.create(in)
  }

  it should "parse Id" in {
    expect("qp:namespace/h1/h2/h3/group/typename#idref",
      x => x.isId,
      Id("namespace", Seq("h1", "h2", "h3"), "group", "typename", "idref")
    )
  }

  it should "parse Id without hierarchy" in {
    expect("qp:namespace/group/typename#idref",
      x => x.isId,
      Id("namespace", Seq(), "group", "typename", "idref")
    )
  }

  it should "parse TypeId" in {
    expect("qp:namespace/h1/h2/h3/group/typename",
      x => x.isTypeId,
      TypeId("namespace", Seq("h1", "h2", "h3"), "group", "typename")
    )
  }

  it should "parse TypeId without hierarchy" in {
    expect("qp:namespace/group/typename",
      x => x.isTypeId,
      TypeId("namespace", Seq(), "group", "typename")
    )
  }

  /*
  it should "parse GroupId" in {
    expect("qp:namespace/h1/h2/h3/group",
      x => x.isGroupId,
      GroupId("namespace", Seq("h1", "h2", "h3"), "group")
    )
  }

  it should "parse GroupId without hierarchy" in {
    expect("qp:namespace/group",
      x => x.isGroupId,
      GroupId("namespace", Seq(), "group")
    )
  }

  it should "parse HierarchyId" in {
    val id = fromString("qp:namespace/h1/h2/h3")
    id.isHierarchyId shouldBe true
    id shouldBe HierarchyId("namespace", Seq("h1", "h2", "h3"))
  }

  it should "parse NamespaceId" in {
    val id = fromString("qp:namespace")
    id.isNamespaceId shouldBe true
    id shouldBe NamespaceId("namespace")
  }*/

}
