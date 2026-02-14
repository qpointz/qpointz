package io.qpointz.mill.source

import io.qpointz.mill.vectors.VectorBlockIterator

/**
 * Extension functions for bridging between row-oriented ([FlowRecordSource])
 * and columnar ([FlowVectorSource]) data paths.
 *
 * These bridges allow transparent conversion: row sources can be consumed
 * as vector blocks and vice versa. When the native path matches the
 * access mode, there is zero overhead.
 */

/**
 * Wraps this [FlowRecordSource] as a [FlowVectorSource] that produces
 * [io.qpointz.mill.proto.VectorBlock] instances by batching records.
 *
 * @param batchSize number of records per vector block (default 1024)
 * @return a [FlowVectorSource] view of this record source
 */
fun FlowRecordSource.asVectorSource(batchSize: Int = 1024): FlowVectorSource {
    val recordSource = this
    return object : FlowVectorSource {
        override val schema: RecordSchema = recordSource.schema

        override fun vectorBlocks(batchSize2: Int): VectorBlockIterator {
            return recordSourceToVectorIterator(
                recordSource,
                schema.toVectorBlockSchema(),
                batchSize2
            )
        }
    }
}

/**
 * Wraps this [FlowVectorSource] as a [FlowRecordSource] that produces
 * [Record] instances by extracting rows from vector blocks.
 *
 * @return a [FlowRecordSource] view of this vector source
 */
fun FlowVectorSource.asRecordSource(): FlowRecordSource {
    val vectorSource = this
    return object : FlowRecordSource {
        override val schema: RecordSchema = vectorSource.schema

        override fun iterator(): Iterator<Record> {
            return vectorSourceToRecords(vectorSource).iterator()
        }
    }
}
