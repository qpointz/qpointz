/*
 * Copyright 2020 qpointz.io
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
 *
 */

package io.qpointz.surface.api.collection

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import io.qpointz.surface.api.collection.CollectionProtocol._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CollectionServiceTest extends AnyFlatSpec with ScalatestRouteTest with Matchers {

  private val testKit = ActorTestKit()
  implicit val sys = testKit.system

  private val cols = Seq(Collection("aaa"), Collection("bbb"))

  private val mockedBehavior = Behaviors.receiveMessage[CollectionCommand] {
    case x: GetCollections => {
      x.replyTo ! Collections(collections = cols)
      Behaviors.same
    }
    case x:CreateCollection => {
      x.replyTo ! x.collection
      Behaviors.same
    }
  }

  val probe = testKit.createTestProbe[CollectionCommand]()
  val mockedPublisher = testKit.spawn(Behaviors.monitor(probe.ref, mockedBehavior))

  val cSvc = new CollectionService(mockedPublisher)

  behavior of "collections"

  it should "return list of collections" in {
    Get("/collections") ~> cSvc.routes ~> check {
      responseAs[Collections] shouldEqual Collections(cols)
    }
  }

  it should "create collection" in {
    val col = Collection(key="kkkk")
    Post("/collections", col) ~> cSvc.routes ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Collection] shouldEqual col
    }
  }



}
