package io.qpointz.mill.source.descriptor

import com.fasterxml.jackson.databind.jsontype.NamedType

/**
 * SPI interface for registering descriptor subtypes with Jackson.
 *
 * Each module (core, format-text, format-excel, format-avro, format-parquet, etc.)
 * provides one or more implementations of this interface in
 * `META-INF/services/io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider`.
 *
 * The [DescriptorModule] discovers all providers via [java.util.ServiceLoader]
 * at initialization time and registers all declared named types.
 *
 * @see DescriptorModule
 */
interface DescriptorSubtypeProvider {

    /**
     * Returns the list of named subtypes this provider contributes.
     *
     * Each [NamedType] pairs a concrete class with a discriminator string
     * (matching its `@JsonTypeName` annotation value).
     *
     * @return list of [NamedType] to register with Jackson
     */
    fun subtypes(): List<NamedType>
}
