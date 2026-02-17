package io.qpointz.mill.source.descriptor

import com.fasterxml.jackson.databind.module.SimpleModule
import java.util.ServiceLoader

/**
 * Jackson module that auto-discovers and registers all descriptor subtypes
 * via the [DescriptorSubtypeProvider] SPI.
 *
 * Usage:
 * ```kotlin
 * val mapper = ObjectMapper(YAMLFactory())
 *     .registerModule(KotlinModule.Builder().build())
 *     .registerModule(DescriptorModule())
 * ```
 *
 * Third-party modules can contribute their own descriptor subtypes simply by
 * providing a `META-INF/services/io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider`
 * file on the classpath.
 *
 * @see DescriptorSubtypeProvider
 */
class DescriptorModule : SimpleModule("MillSourceDescriptorModule") {

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        val providers = ServiceLoader.load(DescriptorSubtypeProvider::class.java)
        for (provider in providers) {
            for (namedType in provider.subtypes()) {
                context.registerSubtypes(namedType)
            }
        }
    }
}
