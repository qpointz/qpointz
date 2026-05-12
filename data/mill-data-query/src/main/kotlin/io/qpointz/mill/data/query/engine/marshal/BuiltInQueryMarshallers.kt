package io.qpointz.mill.data.query.engine.marshal

import tools.jackson.databind.ObjectMapper
import io.qpointz.mill.data.query.engine.QueryFormats
import io.qpointz.mill.proto.VectorBlock
import io.qpointz.mill.sql.RecordReader
import io.qpointz.mill.sql.VectorBlockRecordIterator
import java.io.OutputStream

/**
 * Encodes a global row range from materialized [VectorBlock]s into consumer callbacks.
 */
internal object RowPageEncoder {

    /**
     * @param blocks all blocks for the session.
     * @param globalRowStart inclusive 0-based row offset across the concatenated blocks.
     * @param rowCount number of rows to visit (may be 0).
     * @param consumer invoked for each row after skipping.
     */
    fun forEachRow(
        blocks: List<VectorBlock>,
        globalRowStart: Int,
        rowCount: Int,
        consumer: (RecordReader) -> Unit,
    ) {
        if (rowCount == 0) {
            return
        }
        val reader = VectorBlockRecordIterator.of(blocks.iterator())
        var skipped = 0
        while (skipped < globalRowStart && reader.hasNext()) {
            if (!reader.next()) {
                return
            }
            skipped++
        }
        var emitted = 0
        while (emitted < rowCount && reader.hasNext()) {
            if (!reader.next()) {
                break
            }
            consumer(reader)
            emitted++
        }
    }

    fun rowToMap(reader: RecordReader): LinkedHashMap<String, Any?> {
        val map = LinkedHashMap<String, Any?>()
        val n = reader.columnCount
        for (i in 0 until n) {
            val name = reader.getColumnMetadata(i).name
            map[name] = if (reader.isNull(i)) null else reader.getObject(i)
        }
        return map
    }
}

/**
 * JSON row-array marshaller (`rows-objects`) — writes the **inner** `data` payload (array of row objects).
 */
class RowsObjectsMarshaller(
    private val objectMapper: ObjectMapper = ObjectMapper(),
) : ResultMarshaller {
    override val formatId: String = QueryFormats.ROWS_OBJECTS
    override val contentType: String = "application/json"
    override val acceptedMimeTypes: Set<String> = setOf("application/json", "*/*")

    override fun writePage(blocks: List<VectorBlock>, globalRowStart: Int, rowCount: Int, out: OutputStream) {
        val rows = ArrayList<Map<String, Any?>>(rowCount)
        RowPageEncoder.forEachRow(blocks, globalRowStart, rowCount) { reader ->
            rows.add(RowPageEncoder.rowToMap(reader))
        }
        objectMapper.writeValue(out, rows)
    }
}

/**
 * JSON compact marshaller (`rows-compact-batch`) — writes the **inner** `data` payload (`fields` + `rows`).
 */
class RowsCompactMarshaller(
    private val objectMapper: ObjectMapper = ObjectMapper(),
) : ResultMarshaller {
    override val formatId: String = QueryFormats.ROWS_COMPACT_BATCH
    override val contentType: String = "application/json"
    override val acceptedMimeTypes: Set<String> = setOf("application/json", "*/*")

    override fun writePage(blocks: List<VectorBlock>, globalRowStart: Int, rowCount: Int, out: OutputStream) {
        val fieldNames = ArrayList<String>()
        val rowArrays = ArrayList<List<Any?>>()
        var first = true
        RowPageEncoder.forEachRow(blocks, globalRowStart, rowCount) { reader ->
            val n = reader.columnCount
            if (first) {
                for (i in 0 until n) {
                    fieldNames.add(reader.getColumnMetadata(i).name)
                }
                first = false
            }
            val values = ArrayList<Any?>(n)
            for (i in 0 until n) {
                values.add(if (reader.isNull(i)) null else reader.getObject(i))
            }
            rowArrays.add(values)
        }
        val payload = mapOf("fields" to fieldNames, "rows" to rowArrays)
        objectMapper.writeValue(out, payload)
    }
}

/**
 * SPI provider registering built-in JSON marshallers.
 */
class BuiltInQueryMarshallerProvider : ResultMarshallerProvider {
    override fun marshallers(): List<ResultMarshaller> =
        listOf(RowsObjectsMarshaller(), RowsCompactMarshaller())
}
