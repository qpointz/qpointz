package io.qpointz.mill.data.query.engine.marshal

import io.qpointz.mill.proto.VectorBlock
import java.io.OutputStream

/**
 * Pluggable marshaller for a single presentation page backed by materialized [VectorBlock]s.
 *
 * @property formatId stable id used by HTTP `format` query and session `defaultFormat`.
 * @property contentType authoritative wire `Content-Type` for this marshaller.
 * @property acceptedMimeTypes MIME entries this marshaller can satisfy for `Accept` negotiation.
 */
interface ResultMarshaller {
    val formatId: String
    val contentType: String
    val acceptedMimeTypes: Set<String>

    /**
     * Writes one presentation page to [out] (blocking I/O).
     *
     * @param blocks materialized blocks for the session (read-only for this call).
     * @param globalRowStart inclusive global row offset (0-based across all blocks).
     * @param rowCount number of rows to write for this page (may be shorter on last page).
     * @param out sink for encoded bytes (caller owns flush/close policy).
     */
    fun writePage(
        blocks: List<VectorBlock>,
        globalRowStart: Int,
        rowCount: Int,
        out: OutputStream,
    )
}

/**
 * SPI entry point for third-party and built-in [ResultMarshaller] contributions.
 */
fun interface ResultMarshallerProvider {
    /**
     * @return one or more marshallers (ids must be unique across the merged registry).
     */
    fun marshallers(): List<ResultMarshaller>
}
