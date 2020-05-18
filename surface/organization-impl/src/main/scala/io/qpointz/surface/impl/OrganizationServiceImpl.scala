package io.qpointz.surface.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import io.qpointz.surface.api.{Organization, OrganizationService}

import scala.concurrent.Future

class OrganizationServiceImpl extends OrganizationService {

  override def get(key: String): ServiceCall[String, Organization] = ServiceCall {
    x => Future.successful(Organization(key))
  }

}
