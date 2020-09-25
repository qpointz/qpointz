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

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import scala.concurrent.Future

class CollectionService(repo: ActorRef[CollectionCommand])(implicit val system: ActorSystem[_]) {

  import CollectionProtocol._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getCollections() : Future[Collections] = repo.ask(GetCollections)


  val routes : Route = concat(
    pathPrefix("collections") {
      concat(
            get {
              complete(getCollections())
            },
            post {
              entity(as[Collection]) {
                complete(StatusCodes.Created, _)
              }
            }
          )
        }/*,

    pathPrefix("collection") {
      concat(
        get {},
        delete {},
        put {}
      )
    }
  */
  )

}
