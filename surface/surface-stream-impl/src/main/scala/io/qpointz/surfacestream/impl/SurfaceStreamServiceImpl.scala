package io.qpointz.surfacestream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import io.qpointz.surfacestream.api.SurfaceStreamService
import io.qpointz.surface.api.SurfaceService

import scala.concurrent.Future

/**
  * Implementation of the SurfaceStreamService.
  */
class SurfaceStreamServiceImpl(surfaceService: SurfaceService) extends SurfaceStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(surfaceService.hello(_).invoke()))
  }
}
