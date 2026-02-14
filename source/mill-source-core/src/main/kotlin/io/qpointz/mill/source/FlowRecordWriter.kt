package io.qpointz.mill.source

/**
 * Writes [Record] instances to an underlying destination.
 *
 * Implementations should be used within a try-with-resources / use block:
 * ```
 * writer.use {
 *     it.open()
 *     records.forEach { r -> it.write(r) }
 * }
 * ```
 */
interface FlowRecordWriter : AutoCloseable {
    /** Prepares the writer for accepting records. */
    fun open()

    /** Writes a single [record]. */
    fun write(record: Record)
}
