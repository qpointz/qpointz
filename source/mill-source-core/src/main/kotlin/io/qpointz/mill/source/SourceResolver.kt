package io.qpointz.mill.source

import io.qpointz.mill.source.descriptor.ConflictResolution
import io.qpointz.mill.source.descriptor.ConflictStrategy
import io.qpointz.mill.source.descriptor.SourceDescriptor
import io.qpointz.mill.source.factory.MaterializedReader
import io.qpointz.mill.source.factory.MaterializedSource
import io.qpointz.mill.source.factory.SourceMaterializer

/**
 * Resolves a [MaterializedSource] into a map of logical table names to
 * [SourceTable] instances, applying multi-reader conflict resolution
 * and attribute enrichment.
 */
object SourceResolver {

    fun resolve(source: MaterializedSource): Map<String, SourceTable> {
        val blobSource = source.blobSource
        val conflicts = source.conflicts

        val allBlobs = blobSource.listBlobs().toList()

        data class ReaderTableEntry(
            val rawTableName: String,
            val readerIndex: Int,
            val reader: MaterializedReader,
            val blobs: List<BlobPath>
        )

        val readerEntries = mutableListOf<ReaderTableEntry>()

        for ((readerIndex, reader) in source.readers.withIndex()) {
            val blobsByTable = allBlobs
                .mapNotNull { blob ->
                    val mapping = reader.tableMapper.mapToTable(blob)
                    if (mapping != null) mapping.tableName to blob else null
                }
                .groupBy({ it.first }, { it.second })

            for ((tableName, blobs) in blobsByTable) {
                readerEntries.add(ReaderTableEntry(tableName, readerIndex, reader, blobs))
            }
        }

        val entriesByRawName = readerEntries.groupBy { it.rawTableName }
        val resolvedTables = mutableMapOf<String, MutableList<Pair<MaterializedReader, List<BlobPath>>>>()

        for ((rawName, entries) in entriesByRawName) {
            val hasExplicitRule = conflicts.hasExplicitRule(rawName)
            val isCollision = entries.size > 1

            if (hasExplicitRule) {
                val strategy = conflicts.strategyFor(rawName)
                when (strategy) {
                    ConflictStrategy.UNION -> {
                        val list = resolvedTables.getOrPut(rawName) { mutableListOf() }
                        for (entry in entries) {
                            list.add(entry.reader to entry.blobs)
                        }
                    }
                    ConflictStrategy.REJECT -> {
                        if (isCollision) {
                            val readerDescs = entries.map { "reader[${it.readerIndex}] (${it.reader.type})" }
                            throw IllegalStateException(
                                "Table '$rawName' produced by multiple readers $readerDescs " +
                                "and conflict rule is 'reject'"
                            )
                        }
                        val entry = entries.single()
                        resolvedTables.getOrPut(rawName) { mutableListOf() }
                            .add(entry.reader to entry.blobs)
                    }
                }
            } else if (!isCollision) {
                val entry = entries.single()
                val finalName = applyLabel(rawName, entry.reader.label)
                resolvedTables.getOrPut(finalName) { mutableListOf() }
                    .add(entry.reader to entry.blobs)
            } else {
                val allHaveLabels = entries.all { it.reader.label != null }
                if (allHaveLabels) {
                    for (entry in entries) {
                        val finalName = applyLabel(rawName, entry.reader.label)
                        resolvedTables.getOrPut(finalName) { mutableListOf() }
                            .add(entry.reader to entry.blobs)
                    }
                } else {
                    when (conflicts.default) {
                        ConflictStrategy.UNION -> {
                            val list = resolvedTables.getOrPut(rawName) { mutableListOf() }
                            for (entry in entries) {
                                list.add(entry.reader to entry.blobs)
                            }
                        }
                        ConflictStrategy.REJECT -> {
                            val readerDescs = entries.map { "reader[${it.readerIndex}] (${it.reader.type})" }
                            throw IllegalStateException(
                                "Table '$rawName' produced by multiple readers $readerDescs " +
                                "and default conflict strategy is 'reject'"
                            )
                        }
                    }
                }
            }
        }

        // Build SourceTable for each resolved table, with attribute enrichment
        return resolvedTables.mapValues { (_, readerBlobPairs) ->
            val firstReader = readerBlobPairs.first().first
            val firstBlobs = readerBlobPairs.first().second
            val baseSchema = firstReader.formatHandler.inferSchema(firstBlobs.first(), blobSource)

            // Augment schema with attribute fields (from first reader that has them)
            val extractor = readerBlobPairs.firstNotNullOfOrNull { it.first.attributeExtractor }
            val schema = if (extractor != null) {
                val extraFields = extractor.schemaFields(baseSchema.size)
                RecordSchema(baseSchema.fields + extraFields)
            } else {
                baseSchema
            }

            val sources = readerBlobPairs.flatMap { (reader, blobs) ->
                blobs.map { blob ->
                    val baseSource = reader.formatHandler.createRecordSource(blob, blobSource, baseSchema)
                    if (reader.attributeExtractor != null) {
                        val attrValues = reader.attributeExtractor.extract(blob)
                        AttributeEnrichingRecordSource(baseSource, attrValues, schema)
                    } else {
                        baseSource
                    }
                }
            }

            MultiFileSourceTable(schema, sources)
        }
    }

    fun resolveDescriptor(
        descriptor: SourceDescriptor,
        materializer: SourceMaterializer = SourceMaterializer()
    ): ResolvedSource {
        val materialized = materializer.materialize(descriptor)
        val tables = resolve(materialized)
        return ResolvedSource(materialized, tables)
    }

    private fun applyLabel(tableName: String, label: String?): String =
        if (label != null) "${tableName}_${label}" else tableName
}

/**
 * Holds a fully resolved source: the materialized runtime components
 * and the discovered tables.
 */
data class ResolvedSource(
    val materialized: MaterializedSource,
    val tables: Map<String, SourceTable>
) : AutoCloseable {

    val name: String get() = materialized.name
    operator fun get(tableName: String): SourceTable? = tables[tableName]
    val tableNames: Set<String> get() = tables.keys

    override fun close() {
        materialized.close()
    }
}
