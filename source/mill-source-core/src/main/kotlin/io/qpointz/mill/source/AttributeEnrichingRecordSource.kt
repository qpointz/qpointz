package io.qpointz.mill.source

/**
 * A [FlowRecordSource] decorator that injects extra attribute columns
 * into every record produced by the delegate source.
 *
 * The [extraValues] are computed once per blob (from [TableAttributeExtractor.extract])
 * and merged into each record.
 *
 * @property delegate    the underlying record source
 * @property extraValues attribute name -> value map to inject
 * @property schema      the augmented schema (base fields + attribute fields)
 */
class AttributeEnrichingRecordSource(
    private val delegate: RecordSource,
    private val extraValues: Map<String, Any?>,
    override val schema: RecordSchema
) : FlowRecordSource {

    override fun iterator(): Iterator<Record> {
        val baseIterator = when (delegate) {
            is FlowRecordSource -> delegate.iterator()
            is FlowVectorSource -> vectorSourceToRecordIterator(delegate)
            else -> emptyList<Record>().iterator()
        }

        return object : Iterator<Record> {
            override fun hasNext(): Boolean = baseIterator.hasNext()
            override fun next(): Record {
                val base = baseIterator.next()
                return Record(base.values + extraValues)
            }
        }
    }
}

/**
 * Bridges a [FlowVectorSource] to a record [Iterator] for use in enrichment.
 */
private fun vectorSourceToRecordIterator(source: FlowVectorSource): Iterator<Record> {
    val fieldNames = source.schema.fieldNames
    val blocks = source.vectorBlocks()
    return object : Iterator<Record> {
        private var currentBlock: io.qpointz.mill.proto.VectorBlock? = null
        private var rowIdx = 0
        private var blockSize = 0

        override fun hasNext(): Boolean {
            if (currentBlock != null && rowIdx < blockSize) return true
            if (!blocks.hasNext()) return false
            currentBlock = blocks.next()
            blockSize = currentBlock!!.vectorSize
            rowIdx = 0
            return blockSize > 0
        }

        override fun next(): Record {
            if (!hasNext()) throw NoSuchElementException()
            val block = currentBlock!!
            val values = mutableMapOf<String, Any?>()
            for (vector in block.vectorsList) {
                val fieldIdx = vector.fieldIdx
                if (fieldIdx >= fieldNames.size) continue
                val name = fieldNames[fieldIdx]
                val isNull = vector.hasNulls() &&
                    vector.nulls.nullsList.size > rowIdx &&
                    vector.nulls.nullsList[rowIdx]
                values[name] = if (isNull) null else extractVectorValue(vector, rowIdx)
            }
            rowIdx++
            return Record(values)
        }
    }
}
