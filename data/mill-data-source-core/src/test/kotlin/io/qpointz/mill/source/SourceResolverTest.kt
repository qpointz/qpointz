package io.qpointz.mill.source

import io.qpointz.mill.source.descriptor.*
import io.qpointz.mill.source.factory.SourceMaterializer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Paths

class SourceResolverTest {

    private val materializer = SourceMaterializer()
    private val csvRoot = Paths.get("../../test/datasets/airlines/csv").toAbsolutePath().normalize()
    private val partRoot = Paths.get("../../test/datasets/partitioned/hierarchy").toAbsolutePath().normalize()

    private fun regexTable(pattern: String = ".*?(?<table>[^/]+)\\.csv$") =
        TableDescriptor(mapping = RegexTableMappingDescriptor(pattern = pattern))

    private fun directoryTable(depth: Int = 1) =
        TableDescriptor(mapping = DirectoryTableMappingDescriptor(depth = depth))

    // ------------------------------------------------------------------
    // Single reader (basic resolve)
    // ------------------------------------------------------------------

    @Nested
    inner class SingleReaderTests {

        @Test
        fun shouldResolveTablesFromAirlinesDataset() {
            val descriptor = SourceDescriptor(
                name = "airlines",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(
                        type = "stub",
                        format = StubFormatDescriptor(delimiter = ","),
                        table = regexTable()
                    )
                )
            )
            val materialized = materializer.materialize(descriptor)
            val tables = SourceResolver.resolve(materialized)

            assertEquals(4, tables.size, "Should discover 4 tables: ${tables.keys}")
            assertTrue(tables.containsKey("cities"))
            assertTrue(tables.containsKey("flights"))
            assertTrue(tables.containsKey("passenger"))
            assertTrue(tables.containsKey("segments"))

            materialized.close()
        }

        @Test
        fun shouldReturnRecordsFromResolvedTable() {
            val descriptor = SourceDescriptor(
                name = "airlines",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(
                        type = "stub",
                        format = StubFormatDescriptor(),
                        table = regexTable()
                    )
                )
            )
            val materialized = materializer.materialize(descriptor)
            val tables = SourceResolver.resolve(materialized)

            val citiesRecords = tables["cities"]!!.records().toList()
            assertEquals(1, citiesRecords.size)
            val filename = citiesRecords[0]["filename"] as String
            assertTrue(filename.endsWith("cities.csv"), "Got: $filename")

            materialized.close()
        }

        @Test
        fun shouldResolveEmptyDirectoryToEmptyMap() {
            val tempDir = java.nio.file.Files.createTempDirectory("empty-source-test")
            try {
                val descriptor = SourceDescriptor(
                    name = "empty",
                    storage = LocalStorageDescriptor(rootPath = tempDir.toString()),
                    readers = listOf(
                        ReaderDescriptor(
                            type = "stub",
                            format = StubFormatDescriptor(),
                            table = directoryTable()
                        )
                    )
                )
                val materialized = materializer.materialize(descriptor)
                val tables = SourceResolver.resolve(materialized)
                assertTrue(tables.isEmpty())
                materialized.close()
            } finally {
                java.nio.file.Files.delete(tempDir)
            }
        }
    }

    // ------------------------------------------------------------------
    // Label suffixing
    // ------------------------------------------------------------------

    @Nested
    inner class LabelTests {

        @Test
        fun shouldAppendLabelToTableNames() {
            val descriptor = SourceDescriptor(
                name = "labeled",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(
                        type = "stub",
                        label = "raw",
                        format = StubFormatDescriptor(),
                        table = regexTable()
                    )
                )
            )
            val materialized = materializer.materialize(descriptor)
            val tables = SourceResolver.resolve(materialized)

            assertTrue(tables.containsKey("cities_raw"), "Got: ${tables.keys}")
            assertTrue(tables.containsKey("flights_raw"))
            assertFalse(tables.containsKey("cities"))

            materialized.close()
        }

        @Test
        fun shouldAvoidCollisionWithDifferentLabels() {
            val descriptor = SourceDescriptor(
                name = "dual",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                conflicts = ConflictResolution(default = ConflictStrategy.REJECT),
                readers = listOf(
                    ReaderDescriptor(
                        type = "stub", label = "a",
                        format = StubFormatDescriptor(),
                        table = regexTable()
                    ),
                    ReaderDescriptor(
                        type = "stub", label = "b",
                        format = StubFormatDescriptor(),
                        table = regexTable()
                    )
                )
            )
            val materialized = materializer.materialize(descriptor)
            val tables = SourceResolver.resolve(materialized)

            assertEquals(8, tables.size)
            assertTrue(tables.containsKey("cities_a"))
            assertTrue(tables.containsKey("cities_b"))

            materialized.close()
        }
    }

    // ------------------------------------------------------------------
    // Conflict resolution: reject
    // ------------------------------------------------------------------

    @Nested
    inner class ConflictRejectTests {

        @Test
        fun shouldRejectCollisionByDefault() {
            val descriptor = SourceDescriptor(
                name = "collision",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable()),
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable())
                )
            )
            val materialized = materializer.materialize(descriptor)

            val ex = assertThrows<IllegalStateException> {
                SourceResolver.resolve(materialized)
            }
            assertTrue(ex.message!!.contains("reject"), "Got: ${ex.message}")

            materialized.close()
        }

        @Test
        fun shouldRejectPerTableOverride() {
            val descriptor = SourceDescriptor(
                name = "partial-reject",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                conflicts = ConflictResolution(
                    default = ConflictStrategy.UNION,
                    rules = mapOf("cities" to ConflictStrategy.REJECT)
                ),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable()),
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable())
                )
            )
            val materialized = materializer.materialize(descriptor)

            val ex = assertThrows<IllegalStateException> {
                SourceResolver.resolve(materialized)
            }
            assertTrue(ex.message!!.contains("cities"), "Got: ${ex.message}")

            materialized.close()
        }
    }

    // ------------------------------------------------------------------
    // Conflict resolution: union
    // ------------------------------------------------------------------

    @Nested
    inner class ConflictUnionTests {

        @Test
        fun shouldUnionTablesAcrossReaders() {
            val descriptor = SourceDescriptor(
                name = "union-test",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                conflicts = ConflictResolution(default = ConflictStrategy.UNION),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable()),
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable())
                )
            )
            val materialized = materializer.materialize(descriptor)
            val tables = SourceResolver.resolve(materialized)

            assertEquals(4, tables.size)
            val citiesRecords = tables["cities"]!!.records().toList()
            assertEquals(2, citiesRecords.size, "Union should combine records from both readers")

            materialized.close()
        }

        @Test
        fun shouldUnionPerTableOverride() {
            val descriptor = SourceDescriptor(
                name = "selective-union",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                conflicts = ConflictResolution(
                    default = ConflictStrategy.REJECT,
                    rules = mapOf(
                        "cities" to ConflictStrategy.UNION,
                        "flights" to ConflictStrategy.UNION,
                        "passenger" to ConflictStrategy.UNION,
                        "segments" to ConflictStrategy.UNION
                    )
                ),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable()),
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor(), table = regexTable())
                )
            )
            val materialized = materializer.materialize(descriptor)
            val tables = SourceResolver.resolve(materialized)

            assertEquals(4, tables.size)
            assertEquals(2, tables["cities"]!!.records().toList().size)

            materialized.close()
        }
    }

    // ------------------------------------------------------------------
    // Explicit rule ignores label
    // ------------------------------------------------------------------

    @Nested
    inner class ExplicitRuleIgnoresLabelTests {

        @Test
        fun shouldIgnoreLabelWhenExplicitRuleExists() {
            val descriptor = SourceDescriptor(
                name = "explicit-ignores-label",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                conflicts = ConflictResolution(
                    default = ConflictStrategy.REJECT,
                    rules = mapOf(
                        "cities" to ConflictStrategy.UNION,
                        "flights" to ConflictStrategy.UNION,
                        "passenger" to ConflictStrategy.UNION,
                        "segments" to ConflictStrategy.UNION
                    )
                ),
                readers = listOf(
                    ReaderDescriptor(
                        type = "stub", label = "raw",
                        format = StubFormatDescriptor(),
                        table = regexTable()
                    ),
                    ReaderDescriptor(
                        type = "stub", label = "processed",
                        format = StubFormatDescriptor(),
                        table = regexTable()
                    )
                )
            )
            val materialized = materializer.materialize(descriptor)
            val tables = SourceResolver.resolve(materialized)

            assertTrue(tables.containsKey("cities"), "Should have 'cities' not 'cities_raw', got: ${tables.keys}")
            assertFalse(tables.containsKey("cities_raw"))
            assertFalse(tables.containsKey("cities_processed"))
            assertEquals(2, tables["cities"]!!.records().toList().size)

            materialized.close()
        }
    }

    // ------------------------------------------------------------------
    // resolveDescriptor (convenience)
    // ------------------------------------------------------------------

    @Nested
    inner class ResolveDescriptorTests {

        @Test
        fun shouldResolveDescriptorInOneStep() {
            val descriptor = SourceDescriptor(
                name = "airlines",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(
                        type = "stub",
                        format = StubFormatDescriptor(),
                        table = regexTable()
                    )
                )
            )
            val resolved = SourceResolver.resolveDescriptor(descriptor, materializer)

            assertEquals("airlines", resolved.name)
            assertEquals(4, resolved.tables.size)
            assertEquals(resolved.tables.keys, resolved.tableNames)
            assertNotNull(resolved["cities"])
            assertNull(resolved["nonexistent"])

            resolved.close()
        }

        @Test
        fun shouldBeAutoCloseable() {
            val descriptor = SourceDescriptor(
                name = "airlines",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(
                        type = "stub",
                        format = StubFormatDescriptor(),
                        table = regexTable()
                    )
                )
            )
            SourceResolver.resolveDescriptor(descriptor, materializer).use { resolved ->
                assertTrue(resolved.tableNames.isNotEmpty())
            }
        }
    }

    // ------------------------------------------------------------------
    // Shared default table config
    // ------------------------------------------------------------------

    @Nested
    inner class SharedTableTests {

        @Test
        fun shouldUseSourceLevelTableAsDefault() {
            val descriptor = SourceDescriptor(
                name = "shared-mapping",
                storage = LocalStorageDescriptor(rootPath = partRoot.toString()),
                table = directoryTable(),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor())
                )
            )
            val materialized = materializer.materialize(descriptor)
            val tables = SourceResolver.resolve(materialized)

            assertTrue(tables.isNotEmpty(), "Should discover tables")
            assertTrue(tables.containsKey("01"), "Got: ${tables.keys}")

            materialized.close()
        }
    }

    // ------------------------------------------------------------------
    // Attribute enrichment
    // ------------------------------------------------------------------

    @Nested
    inner class AttributeEnrichmentTests {

        @Test
        fun shouldInjectConstantAttributes() {
            val descriptor = SourceDescriptor(
                name = "enriched",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(
                        type = "stub",
                        format = StubFormatDescriptor(),
                        table = TableDescriptor(
                            mapping = RegexTableMappingDescriptor(
                                pattern = ".*?(?<table>[^/]+)\\.csv$"
                            ),
                            attributes = listOf(
                                TableAttributeDescriptor(
                                    name = "pipeline",
                                    source = AttributeSource.CONSTANT,
                                    value = "raw-ingest"
                                )
                            )
                        )
                    )
                )
            )
            val materialized = materializer.materialize(descriptor)
            val tables = SourceResolver.resolve(materialized)

            val citiesRecords = tables["cities"]!!.records().toList()
            assertEquals(1, citiesRecords.size)
            assertEquals("raw-ingest", citiesRecords[0]["pipeline"])

            // Schema should include the extra field
            val schema = tables["cities"]!!.schema
            assertTrue(schema.fieldNames.contains("pipeline"), "Schema fields: ${schema.fieldNames}")

            materialized.close()
        }

        @Test
        fun shouldInjectRegexAttributes() {
            val descriptor = SourceDescriptor(
                name = "regex-enriched",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(
                        type = "stub",
                        format = StubFormatDescriptor(),
                        table = TableDescriptor(
                            mapping = RegexTableMappingDescriptor(
                                pattern = ".*?(?<table>[^/]+)\\.csv$"
                            ),
                            attributes = listOf(
                                TableAttributeDescriptor(
                                    name = "extension",
                                    source = AttributeSource.REGEX,
                                    pattern = ".*\\.(?<ext>[^.]+)$",
                                    group = "ext"
                                )
                            )
                        )
                    )
                )
            )
            val materialized = materializer.materialize(descriptor)
            val tables = SourceResolver.resolve(materialized)

            val citiesRecords = tables["cities"]!!.records().toList()
            assertEquals("csv", citiesRecords[0]["extension"])

            materialized.close()
        }

        @Test
        fun shouldUseSourceLevelAttributesWhenReaderHasNoTable() {
            val descriptor = SourceDescriptor(
                name = "source-attrs",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                table = TableDescriptor(
                    mapping = RegexTableMappingDescriptor(
                        pattern = ".*?(?<table>[^/]+)\\.csv$"
                    ),
                    attributes = listOf(
                        TableAttributeDescriptor(
                            name = "source_id",
                            source = AttributeSource.CONSTANT,
                            value = "warehouse-01"
                        )
                    )
                ),
                readers = listOf(
                    ReaderDescriptor(type = "stub", format = StubFormatDescriptor())
                )
            )
            val materialized = materializer.materialize(descriptor)
            val tables = SourceResolver.resolve(materialized)

            val citiesRecords = tables["cities"]!!.records().toList()
            assertEquals("warehouse-01", citiesRecords[0]["source_id"])

            materialized.close()
        }

        @Test
        fun shouldOverrideSourceLevelAttributesWithReaderLevel() {
            val descriptor = SourceDescriptor(
                name = "override-attrs",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                table = TableDescriptor(
                    mapping = RegexTableMappingDescriptor(
                        pattern = ".*?(?<table>[^/]+)\\.csv$"
                    ),
                    attributes = listOf(
                        TableAttributeDescriptor(
                            name = "source_attr",
                            source = AttributeSource.CONSTANT,
                            value = "from-source"
                        )
                    )
                ),
                readers = listOf(
                    ReaderDescriptor(
                        type = "stub",
                        format = StubFormatDescriptor(),
                        table = TableDescriptor(
                            mapping = RegexTableMappingDescriptor(
                                pattern = ".*?(?<table>[^/]+)\\.csv$"
                            ),
                            attributes = listOf(
                                TableAttributeDescriptor(
                                    name = "reader_attr",
                                    source = AttributeSource.CONSTANT,
                                    value = "from-reader"
                                )
                            )
                        )
                    )
                )
            )
            val materialized = materializer.materialize(descriptor)
            val tables = SourceResolver.resolve(materialized)

            val citiesRecords = tables["cities"]!!.records().toList()
            // Reader table replaces source table completely
            assertEquals("from-reader", citiesRecords[0]["reader_attr"])
            assertNull(citiesRecords[0]["source_attr"],
                "Source-level attribute should NOT be present when reader overrides table")

            materialized.close()
        }
    }
}
