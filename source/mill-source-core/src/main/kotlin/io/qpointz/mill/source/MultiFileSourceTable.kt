package io.qpointz.mill.source

import io.qpointz.mill.proto.VectorBlock
import io.qpointz.mill.proto.VectorBlockSchema
import io.qpointz.mill.vectors.VectorBlockIterator

/**
 * A [SourceTable] backed by multiple [RecordSource] instances.
 *
 * Each source represents one file/blob. Data is concatenated (unioned)
 * across all sources. The dual-mode design ensures zero-overhead when
 * the access mode matches the native format:
 * - Columnar sources ([FlowVectorSource]) are iterated natively on the
 *   columnar path — no row materialization.
 * - Row sources ([FlowRecordSource]) are iterated natively on the
 *   record path — no vector conversion.
 *
 * When modes are mixed, bridging is deferred to 1.6 (record-vector bridges).
 *
 * @property schema  the schema for this table (all sources must share it)
 * @property sources the underlying per-file record sources
 */
class MultiFileSourceTable(
    override val schema: RecordSchema,
    private val sources: List<RecordSource>
) : SourceTable {

    override fun records(): Iterable<Record> {
        return Iterable {
            sources.asSequence().flatMap { source ->
                when (source) {
                    is FlowRecordSource -> source.asSequence()
                    is FlowVectorSource -> vectorSourceToRecords(source)
                    else -> emptySequence()
                }
            }.iterator()
        }
    }

    override fun vectorBlocks(batchSize: Int): VectorBlockIterator {
        val vbSchema = schema.toVectorBlockSchema()
        return ConcatenatingVectorBlockIterator(vbSchema, sources, batchSize)
    }

    companion object {

        /**
         * Creates a [MultiFileSourceTable] from a single source.
         */
        fun ofSingle(schema: RecordSchema, source: RecordSource): MultiFileSourceTable =
            MultiFileSourceTable(schema, listOf(source))

        /**
         * Creates an empty [MultiFileSourceTable] with no sources.
         */
        fun empty(schema: RecordSchema): MultiFileSourceTable =
            MultiFileSourceTable(schema, emptyList())
    }
}

/**
 * Bridges a [FlowVectorSource] to a [Sequence] of [Record] instances
 * by reading vector blocks and extracting rows.
 */
internal fun vectorSourceToRecords(source: FlowVectorSource): Sequence<Record> {
    return sequence {
        val fieldNames = source.schema.fieldNames
        val iter = source.vectorBlocks()
        while (iter.hasNext()) {
            val block = iter.next()
            for (rowIdx in 0 until block.vectorSize) {
                val values = mutableMapOf<String, Any?>()
                for (vector in block.vectorsList) {
                    val fieldIdx = vector.fieldIdx
                    if (fieldIdx >= fieldNames.size) continue
                    val name = fieldNames[fieldIdx]
                    val isNull = vector.hasNulls() && vector.nulls.nullsList.size > rowIdx && vector.nulls.nullsList[rowIdx]
                    values[name] = if (isNull) null else extractVectorValue(vector, rowIdx)
                }
                yield(Record(values))
            }
        }
    }
}

/**
 * Extracts a single value from a protobuf [io.qpointz.mill.proto.Vector] at [rowIdx].
 */
internal fun extractVectorValue(vector: io.qpointz.mill.proto.Vector, rowIdx: Int): Any? {
    return when {
        vector.hasStringVector() -> vector.stringVector.getValues(rowIdx)
        vector.hasI32Vector() -> vector.i32Vector.getValues(rowIdx)
        vector.hasI64Vector() -> vector.i64Vector.getValues(rowIdx)
        vector.hasFp64Vector() -> vector.fp64Vector.getValues(rowIdx)
        vector.hasFp32Vector() -> vector.fp32Vector.getValues(rowIdx)
        vector.hasBoolVector() -> vector.boolVector.getValues(rowIdx)
        vector.hasByteVector() -> vector.byteVector.getValues(rowIdx).toByteArray()
        else -> null
    }
}

/**
 * A [VectorBlockIterator] that concatenates vector blocks from multiple sources.
 */
internal class ConcatenatingVectorBlockIterator(
    private val vbSchema: VectorBlockSchema,
    private val sources: List<RecordSource>,
    private val batchSize: Int
) : VectorBlockIterator {

    private val sourceIterator = sources.iterator()
    private var currentIterator: VectorBlockIterator? = null

    override fun schema(): VectorBlockSchema = vbSchema

    override fun hasNext(): Boolean {
        while (true) {
            val current = currentIterator
            if (current != null && current.hasNext()) return true
            if (!sourceIterator.hasNext()) return false
            currentIterator = nextVectorIterator(sourceIterator.next())
        }
    }

    override fun next(): VectorBlock {
        if (!hasNext()) throw NoSuchElementException()
        return currentIterator!!.next()
    }

    override fun remove() {
        throw UnsupportedOperationException("remove() is not supported")
    }

    private fun nextVectorIterator(source: RecordSource): VectorBlockIterator {
        return when (source) {
            is FlowVectorSource -> source.vectorBlocks(batchSize)
            is FlowRecordSource -> recordSourceToVectorIterator(source, vbSchema, batchSize)
            else -> emptyVectorBlockIterator(vbSchema)
        }
    }
}

/**
 * Bridges a [FlowRecordSource] to a [VectorBlockIterator] by batching
 * records into vector blocks.
 */
internal fun recordSourceToVectorIterator(
    source: FlowRecordSource,
    vbSchema: VectorBlockSchema,
    batchSize: Int
): VectorBlockIterator {
    val records = source.iterator()
    val schema = source.schema

    return object : VectorBlockIterator {
        override fun schema(): VectorBlockSchema = vbSchema

        override fun hasNext(): Boolean = records.hasNext()

        override fun next(): VectorBlock {
            if (!records.hasNext()) throw NoSuchElementException()

            val batch = mutableListOf<Record>()
            while (records.hasNext() && batch.size < batchSize) {
                batch.add(records.next())
            }

            return buildVectorBlock(vbSchema, schema, batch)
        }

        override fun remove() {
            throw UnsupportedOperationException("remove() is not supported")
        }
    }
}

/**
 * Builds a protobuf [VectorBlock] from a list of [Record] instances.
 */
internal fun buildVectorBlock(
    vbSchema: VectorBlockSchema,
    recordSchema: RecordSchema,
    records: List<Record>
): VectorBlock {
    val builder = VectorBlock.newBuilder()
        .setSchema(vbSchema)
        .setVectorSize(records.size)

    for (field in recordSchema.fields) {
        val vectorBuilder = io.qpointz.mill.proto.Vector.newBuilder()
            .setFieldIdx(field.index)

        val nullsBuilder = io.qpointz.mill.proto.Vector.NullsVector.newBuilder()
        var hasAnyNulls = false

        val values = records.map { it[field.name] }

        for (v in values) {
            val isNull = v == null
            nullsBuilder.addNulls(isNull)
            if (isNull) hasAnyNulls = true
        }

        if (hasAnyNulls) {
            vectorBuilder.setNulls(nullsBuilder)
        }

        setVectorValues(vectorBuilder, field, values)
        builder.addVectors(vectorBuilder)
    }

    return builder.build()
}

/**
 * Sets the typed values on a [io.qpointz.mill.proto.Vector.Builder] based on the field's logical type.
 */
internal fun setVectorValues(
    vectorBuilder: io.qpointz.mill.proto.Vector.Builder,
    field: SchemaField,
    values: List<Any?>
) {
    val typeId = field.type.asLogicalDataType().typeId

    when (typeId) {
        io.qpointz.mill.proto.LogicalDataType.LogicalDataTypeId.STRING -> {
            val b = io.qpointz.mill.proto.Vector.StringVector.newBuilder()
            values.forEach { b.addValues(it?.toString() ?: "") }
            vectorBuilder.setStringVector(b)
        }
        io.qpointz.mill.proto.LogicalDataType.LogicalDataTypeId.INT,
        io.qpointz.mill.proto.LogicalDataType.LogicalDataTypeId.SMALL_INT,
        io.qpointz.mill.proto.LogicalDataType.LogicalDataTypeId.TINY_INT -> {
            val b = io.qpointz.mill.proto.Vector.I32Vector.newBuilder()
            values.forEach { b.addValues((it as? Number)?.toInt() ?: 0) }
            vectorBuilder.setI32Vector(b)
        }
        io.qpointz.mill.proto.LogicalDataType.LogicalDataTypeId.BIG_INT -> {
            val b = io.qpointz.mill.proto.Vector.I64Vector.newBuilder()
            values.forEach { b.addValues((it as? Number)?.toLong() ?: 0L) }
            vectorBuilder.setI64Vector(b)
        }
        io.qpointz.mill.proto.LogicalDataType.LogicalDataTypeId.FLOAT -> {
            val b = io.qpointz.mill.proto.Vector.FP32Vector.newBuilder()
            values.forEach { b.addValues((it as? Number)?.toFloat() ?: 0f) }
            vectorBuilder.setFp32Vector(b)
        }
        io.qpointz.mill.proto.LogicalDataType.LogicalDataTypeId.DOUBLE -> {
            val b = io.qpointz.mill.proto.Vector.FP64Vector.newBuilder()
            values.forEach { b.addValues((it as? Number)?.toDouble() ?: 0.0) }
            vectorBuilder.setFp64Vector(b)
        }
        io.qpointz.mill.proto.LogicalDataType.LogicalDataTypeId.BOOL -> {
            val b = io.qpointz.mill.proto.Vector.BoolVector.newBuilder()
            values.forEach { b.addValues(it as? Boolean ?: false) }
            vectorBuilder.setBoolVector(b)
        }
        io.qpointz.mill.proto.LogicalDataType.LogicalDataTypeId.BINARY -> {
            val b = io.qpointz.mill.proto.Vector.BytesVector.newBuilder()
            values.forEach {
                val bytes = when (it) {
                    is ByteArray -> com.google.protobuf.ByteString.copyFrom(it)
                    else -> com.google.protobuf.ByteString.EMPTY
                }
                b.addValues(bytes)
            }
            vectorBuilder.setByteVector(b)
        }
        else -> {
            // Date, Time, Timestamp, etc. — encode as string for now
            val b = io.qpointz.mill.proto.Vector.StringVector.newBuilder()
            values.forEach { b.addValues(it?.toString() ?: "") }
            vectorBuilder.setStringVector(b)
        }
    }
}

/**
 * Returns an empty [VectorBlockIterator] that yields no blocks.
 */
internal fun emptyVectorBlockIterator(schema: VectorBlockSchema): VectorBlockIterator {
    return object : VectorBlockIterator {
        override fun schema(): VectorBlockSchema = schema
        override fun hasNext(): Boolean = false
        override fun next(): VectorBlock = throw NoSuchElementException()
        override fun remove() { throw UnsupportedOperationException("remove() is not supported") }
    }
}
