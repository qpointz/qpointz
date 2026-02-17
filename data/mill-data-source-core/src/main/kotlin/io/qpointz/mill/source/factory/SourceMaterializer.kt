package io.qpointz.mill.source.factory

import io.qpointz.mill.source.*
import io.qpointz.mill.source.descriptor.*
import io.qpointz.mill.source.verify.*
import java.util.ServiceLoader

/**
 * Materializes runtime objects from declarative [SourceDescriptor]s.
 *
 * Uses [ServiceLoader] to discover factory implementations for each
 * descriptor type. Third-party modules contribute factories via SPI.
 *
 * @param classLoader optional class loader for SPI discovery
 */
class SourceMaterializer(
    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
) {

    private val storageFactories: Map<Class<out StorageDescriptor>, StorageFactory> by lazy {
        ServiceLoader.load(StorageFactory::class.java, classLoader)
            .associateBy { it.descriptorType }
    }

    private val formatHandlerFactories: Map<Class<out FormatDescriptor>, FormatHandlerFactory> by lazy {
        ServiceLoader.load(FormatHandlerFactory::class.java, classLoader)
            .associateBy { it.descriptorType }
    }

    private val tableMapperFactories: Map<Class<out TableMappingDescriptor>, TableMapperFactory> by lazy {
        ServiceLoader.load(TableMapperFactory::class.java, classLoader)
            .associateBy { it.descriptorType }
    }

    fun createBlobSource(descriptor: StorageDescriptor): BlobSource {
        val factory = storageFactories[descriptor::class.java]
            ?: throw IllegalArgumentException(
                "No StorageFactory registered for ${descriptor::class.java.name}. " +
                "Available: ${storageFactories.keys.map { it.name }}"
            )
        return factory.create(descriptor)
    }

    fun createFormatHandler(descriptor: FormatDescriptor): FormatHandler {
        val factory = formatHandlerFactories[descriptor::class.java]
            ?: throw IllegalArgumentException(
                "No FormatHandlerFactory registered for ${descriptor::class.java.name}. " +
                "Available: ${formatHandlerFactories.keys.map { it.name }}"
            )
        return factory.create(descriptor)
    }

    fun createTableMapper(descriptor: TableMappingDescriptor): BlobToTableMapper {
        val factory = tableMapperFactories[descriptor::class.java]
            ?: throw IllegalArgumentException(
                "No TableMapperFactory registered for ${descriptor::class.java.name}. " +
                "Available: ${tableMapperFactories.keys.map { it.name }}"
            )
        return factory.create(descriptor)
    }

    /**
     * Resolves the effective [TableDescriptor] for a reader: reader-level
     * replaces source-level entirely if present.
     */
    private fun resolveTable(
        readerTable: TableDescriptor?,
        sourceTable: TableDescriptor?
    ): TableDescriptor? = readerTable ?: sourceTable

    /**
     * Materializes a single [ReaderDescriptor] into a [MaterializedReader].
     */
    fun materializeReader(
        reader: ReaderDescriptor,
        sourceTable: TableDescriptor?
    ): MaterializedReader {
        val effectiveTable = resolveTable(reader.table, sourceTable)

        val mappingDescriptor = effectiveTable?.mapping
            ?: throw IllegalArgumentException(
                "Reader '${reader.type}' has no table mapping " +
                "(neither reader-level nor source-level 'table.mapping' is defined)"
            )

        val attributeExtractor = if (effectiveTable.attributes.isNotEmpty()) {
            TableAttributeExtractor(effectiveTable.attributes)
        } else {
            null
        }

        return MaterializedReader(
            type = reader.type,
            label = reader.label,
            formatHandler = createFormatHandler(reader.format),
            tableMapper = createTableMapper(mappingDescriptor),
            attributeExtractor = attributeExtractor
        )
    }

    /**
     * Materializes all runtime components from a [SourceDescriptor].
     */
    fun materialize(descriptor: SourceDescriptor): MaterializedSource {
        val blobSource = createBlobSource(descriptor.storage)
        val readers = descriptor.readers.map { reader ->
            materializeReader(reader, descriptor.table)
        }
        return MaterializedSource(
            name = descriptor.name,
            blobSource = blobSource,
            readers = readers,
            conflicts = descriptor.conflicts
        )
    }
}

/**
 * A materialized reader: format handler + table mapper + optional attribute extractor.
 */
data class MaterializedReader(
    val type: String,
    val label: String?,
    val formatHandler: FormatHandler,
    val tableMapper: BlobToTableMapper,
    val attributeExtractor: TableAttributeExtractor? = null
) {

    /**
     * Verifies this reader against a set of blobs.
     */
    fun verify(blobs: List<BlobPath>, blobSource: BlobSource, readerIndex: Int = 0): VerificationReport {
        val issues = mutableListOf<VerificationIssue>()
        val tables = mutableListOf<TableSummary>()
        val ctx = mapOf("readerIndex" to readerIndex.toString(), "readerType" to type)

        val blobsByTable = try {
            blobs
                .mapNotNull { blob ->
                    val mapping = tableMapper.mapToTable(blob)
                    if (mapping != null) mapping.tableName to blob else null
                }
                .groupBy({ it.first }, { it.second })
        } catch (e: Exception) {
            issues += VerificationIssue(Severity.ERROR, Phase.TABLE_MAPPING,
                "Reader[$readerIndex] ($type): table mapping failed: ${e.message}", ctx)
            return VerificationReport(issues = issues)
        }

        if (blobsByTable.isEmpty()) {
            issues += VerificationIssue(Severity.WARNING, Phase.TABLE_MAPPING,
                "Reader[$readerIndex] ($type): no blobs matched the table mapping", ctx)
        }

        val mappedBlobs = blobsByTable.values.flatten().toSet()
        val unmappedCount = blobs.size - mappedBlobs.size
        if (unmappedCount > 0) {
            issues += VerificationIssue(Severity.INFO, Phase.TABLE_MAPPING,
                "Reader[$readerIndex] ($type): $unmappedCount blob(s) did not match the table mapping and will be skipped",
                ctx + ("unmappedCount" to unmappedCount.toString()))
        }

        for ((tableName, tableBlobs) in blobsByTable) {
            val tableCtx = ctx + ("tableName" to tableName)
            val schema = try {
                formatHandler.inferSchema(tableBlobs.first(), blobSource)
            } catch (e: Exception) {
                issues += VerificationIssue(Severity.ERROR, Phase.SCHEMA,
                    "Reader[$readerIndex] ($type): schema inference failed for table '$tableName': ${e.message}",
                    tableCtx + ("blob" to tableBlobs.first().uri.toString()))
                null
            }

            // Verify attribute extraction on first blob
            if (attributeExtractor != null && tableBlobs.isNotEmpty()) {
                val attrValues = attributeExtractor.extract(tableBlobs.first())
                val nullAttrs = attrValues.filter { it.value == null }.keys
                if (nullAttrs.isNotEmpty()) {
                    issues += VerificationIssue(Severity.WARNING, Phase.SCHEMA,
                        "Reader[$readerIndex] ($type): attribute extraction returned null for " +
                            "$nullAttrs on table '$tableName' (sample blob: ${tableBlobs.first().uri})",
                        tableCtx)
                }
            }

            tables += TableSummary(
                name = tableName,
                blobCount = tableBlobs.size,
                readerType = type,
                readerLabel = label,
                schema = schema
            )
        }

        return VerificationReport(issues = issues, tables = tables)
    }
}

/**
 * The result of materializing a [SourceDescriptor].
 */
data class MaterializedSource(
    val name: String,
    val blobSource: BlobSource,
    val readers: List<MaterializedReader>,
    val conflicts: ConflictResolution
) : AutoCloseable, Verifiable {

    override fun close() {
        blobSource.close()
    }

    override fun verify(): VerificationReport {
        val issues = mutableListOf<VerificationIssue>()

        val allBlobs = try {
            blobSource.listBlobs().toList()
        } catch (e: Exception) {
            issues += VerificationIssue(Severity.ERROR, Phase.STORAGE,
                "Failed to list blobs: ${e.message}")
            return VerificationReport(issues = issues)
        }

        if (allBlobs.isEmpty()) {
            issues += VerificationIssue(Severity.WARNING, Phase.STORAGE,
                "Storage is empty — no blobs found")
            return VerificationReport(issues = issues)
        }

        issues += VerificationIssue(Severity.INFO, Phase.STORAGE,
            "Storage contains ${allBlobs.size} blob(s)")

        var report = VerificationReport(issues = issues)
        val readerReports = readers.mapIndexed { index, reader ->
            reader.verify(allBlobs, blobSource, index).also { report += it }
        }

        val allTableSummaries = readerReports.flatMap { it.tables }
        val tablesByName = allTableSummaries.groupBy { it.name }

        val resolvedIssues = mutableListOf<VerificationIssue>()
        val resolvedTables = mutableListOf<TableSummary>()

        for ((rawName, summaries) in tablesByName) {
            val hasExplicitRule = conflicts.hasExplicitRule(rawName)
            val isCollision = summaries.size > 1

            if (!isCollision) {
                val s = summaries.single()
                val finalName = if (s.readerLabel != null) "${rawName}_${s.readerLabel}" else rawName
                resolvedTables += s.copy(name = finalName)
                continue
            }

            if (hasExplicitRule) {
                val strategy = conflicts.strategyFor(rawName)
                when (strategy) {
                    ConflictStrategy.UNION -> {
                        resolvedIssues += VerificationIssue(Severity.INFO, Phase.CONFLICT,
                            "Table '$rawName': collision resolved by explicit 'union' rule — " +
                                "files from ${summaries.size} reader(s) will be merged",
                            mapOf("tableName" to rawName, "strategy" to "union"))
                        resolvedTables += summaries.first().copy(
                            name = rawName,
                            blobCount = summaries.sumOf { it.blobCount },
                            resolution = "union")
                    }
                    ConflictStrategy.REJECT -> {
                        resolvedIssues += VerificationIssue(Severity.ERROR, Phase.CONFLICT,
                            "Table '$rawName': produced by ${summaries.size} reader(s) " +
                                "and explicit conflict rule is 'reject'",
                            mapOf("tableName" to rawName, "strategy" to "reject"))
                    }
                }
            } else {
                val allHaveLabels = summaries.all { it.readerLabel != null }
                if (allHaveLabels) {
                    val labeledNames = summaries.map { "${rawName}_${it.readerLabel}" }
                    val uniqueNames = labeledNames.toSet()
                    if (uniqueNames.size == labeledNames.size) {
                        resolvedIssues += VerificationIssue(Severity.INFO, Phase.CONFLICT,
                            "Table '$rawName': collision resolved by labels -> ${uniqueNames.sorted()}",
                            mapOf("tableName" to rawName, "strategy" to "label"))
                        for (s in summaries) {
                            resolvedTables += s.copy(
                                name = "${rawName}_${s.readerLabel}", resolution = "label")
                        }
                    } else {
                        resolvedIssues += VerificationIssue(Severity.ERROR, Phase.CONFLICT,
                            "Table '$rawName': labels produce duplicate names $labeledNames",
                            mapOf("tableName" to rawName))
                    }
                } else {
                    when (conflicts.default) {
                        ConflictStrategy.UNION -> {
                            resolvedIssues += VerificationIssue(Severity.INFO, Phase.CONFLICT,
                                "Table '$rawName': collision resolved by default 'union' strategy — " +
                                    "files from ${summaries.size} reader(s) will be merged",
                                mapOf("tableName" to rawName, "strategy" to "union"))
                            resolvedTables += summaries.first().copy(
                                name = rawName,
                                blobCount = summaries.sumOf { it.blobCount },
                                resolution = "union")
                        }
                        ConflictStrategy.REJECT -> {
                            resolvedIssues += VerificationIssue(Severity.ERROR, Phase.CONFLICT,
                                "Table '$rawName': produced by ${summaries.mapIndexed { i, s -> "reader[$i] (${s.readerType})" }} " +
                                    "and default conflict strategy is 'reject'",
                                mapOf("tableName" to rawName, "strategy" to "reject"))
                        }
                    }
                }
            }
        }

        for (ruleName in conflicts.rules.keys) {
            if (ruleName !in tablesByName.keys) {
                resolvedIssues += VerificationIssue(Severity.WARNING, Phase.CONFLICT,
                    "Conflict rule for table '$ruleName' will never apply — " +
                        "no reader discovered a table with that name",
                    mapOf("tableName" to ruleName))
            }
        }

        report += VerificationReport(issues = resolvedIssues, tables = resolvedTables)
        return report
    }
}
