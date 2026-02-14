package io.qpointz.mill.source.discovery

import io.qpointz.mill.source.*
import io.qpointz.mill.source.descriptor.ConflictStrategy
import io.qpointz.mill.source.descriptor.SourceDescriptor
import io.qpointz.mill.source.factory.MaterializedReader
import io.qpointz.mill.source.factory.MaterializedSource
import io.qpointz.mill.source.factory.SourceMaterializer
import io.qpointz.mill.source.verify.Phase
import io.qpointz.mill.source.verify.Severity
import io.qpointz.mill.source.verify.VerificationIssue

/**
 * Non-failing source discovery pipeline.
 *
 * Runs the full chain (materialize, list blobs, map tables, infer schemas)
 * and returns a [DiscoveryResult] with structured findings. **Never throws** —
 * each step is wrapped in try/catch so partial results are always returned.
 *
 * ```kotlin
 * val result = SourceDiscovery.discover(descriptor)
 * result.tables.forEach { table ->
 *     println("${table.name}: ${table.blobPaths.size} files, ${table.schema?.size ?: 0} columns")
 * }
 * result.issues.forEach { issue ->
 *     println("[${issue.severity}] ${issue.message}")
 * }
 * ```
 */
object SourceDiscovery {

    /**
     * Discovers tables from a [SourceDescriptor].
     *
     * Materializes runtime components, runs discovery, and closes the
     * materialized source afterwards. If materialization fails, returns
     * a result with just the error.
     *
     * @param descriptor the source descriptor to discover from
     * @param options    discovery configuration (sample records, etc.)
     * @param materializer the materializer to use (default: SPI-based)
     * @return discovery result — never throws
     */
    fun discover(
        descriptor: SourceDescriptor,
        options: DiscoveryOptions = DiscoveryOptions(),
        materializer: SourceMaterializer = SourceMaterializer()
    ): DiscoveryResult {
        val materialized = try {
            materializer.materialize(descriptor)
        } catch (e: Exception) {
            return DiscoveryResult.failed(
                VerificationIssue(
                    severity = Severity.ERROR,
                    phase = Phase.READER,
                    message = "Materialization failed: ${e.message}"
                )
            )
        }

        return try {
            discover(materialized, options)
        } finally {
            try {
                materialized.close()
            } catch (_: Exception) {
                // ignore close failures
            }
        }
    }

    /**
     * Discovers tables from an already-materialized source.
     *
     * Does **not** close the materialized source — caller retains ownership.
     *
     * @param source  the materialized source to discover from
     * @param options discovery configuration
     * @return discovery result — never throws
     */
    fun discover(
        source: MaterializedSource,
        options: DiscoveryOptions = DiscoveryOptions()
    ): DiscoveryResult {
        val issues = mutableListOf<VerificationIssue>()
        val blobSource = source.blobSource

        // --- Step 1: list blobs ---
        val allBlobs = try {
            blobSource.listBlobs().toList()
        } catch (e: Exception) {
            issues += VerificationIssue(
                Severity.ERROR, Phase.STORAGE,
                "Failed to list blobs: ${e.message}"
            )
            return DiscoveryResult(issues = issues)
        }

        if (allBlobs.isEmpty()) {
            issues += VerificationIssue(
                Severity.INFO, Phase.STORAGE,
                "Storage is empty — no blobs found"
            )
            return DiscoveryResult(issues = issues, blobCount = 0)
        }

        issues += VerificationIssue(
            Severity.INFO, Phase.STORAGE,
            "Storage contains ${allBlobs.size} blob(s)"
        )

        // --- Step 2: map blobs to tables per reader ---
        data class ReaderTableEntry(
            val rawTableName: String,
            val readerIndex: Int,
            val reader: MaterializedReader,
            val blobs: List<BlobPath>
        )

        val readerEntries = mutableListOf<ReaderTableEntry>()
        val allMappedBlobs = mutableSetOf<BlobPath>()

        for ((readerIndex, reader) in source.readers.withIndex()) {
            try {
                val blobsByTable = allBlobs
                    .mapNotNull { blob ->
                        try {
                            val mapping = reader.tableMapper.mapToTable(blob)
                            if (mapping != null) mapping.tableName to blob else null
                        } catch (e: Exception) {
                            issues += VerificationIssue(
                                Severity.WARNING, Phase.TABLE_MAPPING,
                                "Reader[$readerIndex] (${reader.type}): table mapping failed for blob '${blob.uri}': ${e.message}",
                                mapOf("readerIndex" to readerIndex.toString(), "blob" to blob.uri.toString())
                            )
                            null
                        }
                    }
                    .groupBy({ it.first }, { it.second })

                for ((tableName, blobs) in blobsByTable) {
                    readerEntries.add(ReaderTableEntry(tableName, readerIndex, reader, blobs))
                    allMappedBlobs.addAll(blobs)
                }
            } catch (e: Exception) {
                issues += VerificationIssue(
                    Severity.ERROR, Phase.TABLE_MAPPING,
                    "Reader[$readerIndex] (${reader.type}): table mapping failed entirely: ${e.message}",
                    mapOf("readerIndex" to readerIndex.toString())
                )
            }
        }

        val unmappedCount = allBlobs.size - allMappedBlobs.size
        if (unmappedCount > 0) {
            issues += VerificationIssue(
                Severity.INFO, Phase.TABLE_MAPPING,
                "$unmappedCount blob(s) did not match any reader's table mapping"
            )
        }

        // --- Step 3: resolve table name collisions ---
        val entriesByRawName = readerEntries.groupBy { it.rawTableName }
        val resolvedEntries = mutableMapOf<String, MutableList<ReaderTableEntry>>()

        for ((rawName, entries) in entriesByRawName) {
            val hasExplicitRule = source.conflicts.hasExplicitRule(rawName)
            val isCollision = entries.size > 1

            if (hasExplicitRule) {
                val strategy = source.conflicts.strategyFor(rawName)
                when (strategy) {
                    ConflictStrategy.UNION -> {
                        resolvedEntries.getOrPut(rawName) { mutableListOf() }.addAll(entries)
                    }
                    ConflictStrategy.REJECT -> {
                        if (isCollision) {
                            issues += VerificationIssue(
                                Severity.ERROR, Phase.CONFLICT,
                                "Table '$rawName': produced by ${entries.size} reader(s) and conflict rule is 'reject'",
                                mapOf("tableName" to rawName)
                            )
                        } else {
                            resolvedEntries.getOrPut(rawName) { mutableListOf() }.addAll(entries)
                        }
                    }
                }
            } else if (!isCollision) {
                val entry = entries.single()
                val finalName = applyLabel(rawName, entry.reader.label)
                resolvedEntries.getOrPut(finalName) { mutableListOf() }.add(entry)
            } else {
                val allHaveLabels = entries.all { it.reader.label != null }
                if (allHaveLabels) {
                    for (entry in entries) {
                        val finalName = applyLabel(rawName, entry.reader.label)
                        resolvedEntries.getOrPut(finalName) { mutableListOf() }.add(entry)
                    }
                } else {
                    when (source.conflicts.default) {
                        ConflictStrategy.UNION -> {
                            resolvedEntries.getOrPut(rawName) { mutableListOf() }.addAll(entries)
                        }
                        ConflictStrategy.REJECT -> {
                            issues += VerificationIssue(
                                Severity.ERROR, Phase.CONFLICT,
                                "Table '$rawName': produced by ${entries.size} reader(s) and default conflict strategy is 'reject'",
                                mapOf("tableName" to rawName)
                            )
                        }
                    }
                }
            }
        }

        // --- Step 4: infer schemas and collect samples ---
        val tables = mutableListOf<DiscoveredTable>()

        for ((tableName, entries) in resolvedEntries) {
            val allBlobsForTable = entries.flatMap { it.blobs }
            val firstEntry = entries.first()
            val firstBlob = allBlobsForTable.firstOrNull()

            if (firstBlob == null) {
                issues += VerificationIssue(
                    Severity.WARNING, Phase.SCHEMA,
                    "Table '$tableName': no blobs available for schema inference",
                    mapOf("tableName" to tableName)
                )
                tables += DiscoveredTable(
                    name = tableName,
                    schema = null,
                    blobPaths = emptyList(),
                    readerType = firstEntry.reader.type,
                    readerLabel = firstEntry.reader.label
                )
                continue
            }

            val schema = try {
                firstEntry.reader.formatHandler.inferSchema(firstBlob, blobSource)
            } catch (e: Exception) {
                issues += VerificationIssue(
                    Severity.ERROR, Phase.SCHEMA,
                    "Table '$tableName': schema inference failed: ${e.message}",
                    mapOf("tableName" to tableName, "blob" to firstBlob.uri.toString())
                )
                null
            }

            val samples = if (options.maxSampleRecords > 0 && schema != null) {
                readSamples(firstEntry.reader.formatHandler, firstBlob, blobSource, schema, options.maxSampleRecords, issues, tableName)
            } else {
                emptyList()
            }

            tables += DiscoveredTable(
                name = tableName,
                schema = schema,
                blobPaths = allBlobsForTable,
                readerType = firstEntry.reader.type,
                readerLabel = firstEntry.reader.label,
                sampleRecords = samples
            )
        }

        return DiscoveryResult(
            tables = tables,
            issues = issues,
            blobCount = allBlobs.size,
            unmappedBlobCount = unmappedCount
        )
    }

    /**
     * Reads sample records from the first blob of a table.
     * Never throws — returns empty list on failure.
     */
    private fun readSamples(
        formatHandler: FormatHandler,
        blob: BlobPath,
        blobSource: BlobSource,
        schema: RecordSchema,
        maxRecords: Int,
        issues: MutableList<VerificationIssue>,
        tableName: String
    ): List<Record> {
        return try {
            val source = formatHandler.createRecordSource(blob, blobSource, schema)
            if (source is FlowRecordSource) {
                source.asSequence().take(maxRecords).toList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            issues += VerificationIssue(
                Severity.WARNING, Phase.SCHEMA,
                "Table '$tableName': failed to read sample records: ${e.message}",
                mapOf("tableName" to tableName, "blob" to blob.uri.toString())
            )
            emptyList()
        }
    }

    private fun applyLabel(tableName: String, label: String?): String =
        if (label != null) "${tableName}_${label}" else tableName
}
