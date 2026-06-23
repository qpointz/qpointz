package io.qpointz.mill.data.odata.service.config

import com.sdl.odata.edm.EdmConfiguration
import com.sdl.odata.parser.ParserConfiguration
import com.sdl.odata.processor.ProcessorConfiguration
import com.sdl.odata.renderer.RendererConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

/**
 * RWS Spring wiring without the Pekko actor system used by [com.sdl.odata.service.ODataServiceConfiguration].
 */
@Configuration
@Import(
    EdmConfiguration::class,
    ParserConfiguration::class,
    ProcessorConfiguration::class,
    RendererConfiguration::class,
)
class MillODataRwsConfiguration
