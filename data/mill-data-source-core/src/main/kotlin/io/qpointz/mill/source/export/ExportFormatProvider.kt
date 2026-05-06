package io.qpointz.mill.source.export

/**
 * JVM [java.util.ServiceLoader] provider entry for a single export format.
 */
interface ExportFormatProvider {

    /** Descriptor surfaced to HTTP clients. */
    fun metadata(): ExportFormatMetadata

    /** Encoder instance; may be a new object per call. */
    fun encoder(): StreamingExportEncoder
}
