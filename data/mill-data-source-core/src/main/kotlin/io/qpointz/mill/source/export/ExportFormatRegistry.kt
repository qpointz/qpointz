package io.qpointz.mill.source.export

/**
 * Resolved view of all [ExportFormatProvider] instances on the classpath.
 */
interface ExportFormatRegistry {

    /** Returns the provider for [id], or `null` if unknown. */
    fun get(id: String): ExportFormatProvider?

    /** All registered providers (SPI aggregate). */
    fun allProviders(): List<ExportFormatProvider>
}
