package io.qpointz.surface.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait OrganizationService extends Service {

  def get(key:String):ServiceCall[String, Organization]

  override def descriptor: Descriptor = {
    import Service._
    named("organization")
      .withCalls(
        restCall(Method.GET, "/api/organization/:key", get _)
      )
      .withAutoAcl(true)
  }
}
