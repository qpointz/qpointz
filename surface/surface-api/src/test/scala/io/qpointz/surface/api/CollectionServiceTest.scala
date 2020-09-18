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

package io.qpointz.surface.api

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import CollectionProtocol._
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import io.qpointz.surface.api.CollectionRepository.{Command, GetCollections}

class CollectionServiceTest extends AnyFlatSpec with ScalatestRouteTest with Matchers {

  val testKit = ActorTestKit()
  implicit val s = testKit.system

  val mockedBehavior = Behaviors.receiveMessage[Command] { msg =>
    msg match {
      case x: GetCollections => {
        x.replyTo ! Collections(collections = Seq())
        Behaviors.same
      }
    }
  }

  val probe = testKit.createTestProbe[Command]()
  val mockedPublisher = testKit.spawn(Behaviors.monitor(probe.ref, mockedBehavior))

  val cSvc = new CollectionService(mockedPublisher)

  behavior of "collections"

  it should "return list of collections" in {
    Get("/collections") ~> cSvc.routes ~> check {
      //status shouldEqual 200
      //responseAs[Collections] shouldEqual Collections(collections = Seq())
    }
  }

  it should "create collection" in {
    Post("/collections", Collection(key="kkkk")) ~> cSvc.routes ~> check {

    }
  }



}
