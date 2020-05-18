package io.qpointz.surfacestream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import io.qpointz.surfacestream.api.SurfaceStreamService
import io.qpointz.surface.api.SurfaceService
import com.softwaremill.macwire._

class SurfaceStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new SurfaceStreamApplication(context) {
      override def serviceLocator: NoServiceLocator.type = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new SurfaceStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[SurfaceStreamService])
}

abstract class SurfaceStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[SurfaceStreamService](wire[SurfaceStreamServiceImpl])

  // Bind the SurfaceService client
  lazy val surfaceService: SurfaceService = serviceClient.implement[SurfaceService]
}
