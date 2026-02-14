package io.qpointz.mill.source.descriptor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DescriptorSerializationTest {

    private val yamlMapper = SourceObjectMapper.yaml
    private val jsonMapper = SourceObjectMapper.json

    // ------------------------------------------------------------------
    // StorageDescriptor
    // ------------------------------------------------------------------

    @Nested
    inner class StorageDescriptorTests {

        @Test
        fun shouldSerializeLocalStorageDescriptorToYaml() {
            val desc: StorageDescriptor = LocalStorageDescriptor(rootPath = "/data/airlines")
            val yaml = yamlMapper.writerFor(StorageDescriptor::class.java).writeValueAsString(desc)
            assertTrue(yaml.contains("type: \"local\"") || yaml.contains("type: local"), "Should contain type discriminator, got: $yaml")
            assertTrue(yaml.contains("rootPath"), "Should contain rootPath")
        }

        @Test
        fun shouldDeserializeLocalStorageDescriptorFromYaml() {
            val yaml = """
                type: local
                rootPath: /data/airlines
            """.trimIndent()
            val desc = yamlMapper.readValue(yaml, StorageDescriptor::class.java)
            assertInstanceOf(LocalStorageDescriptor::class.java, desc)
            assertEquals("/data/airlines", (desc as LocalStorageDescriptor).rootPath)
        }

        @Test
        fun shouldRoundTripLocalStorageDescriptorJson() {
            val original = LocalStorageDescriptor(rootPath = "/tmp/test")
            val json = jsonMapper.writeValueAsString(original)
            val restored = jsonMapper.readValue(json, StorageDescriptor::class.java)
            assertEquals(original, restored)
        }
    }

    // ------------------------------------------------------------------
    // TableMappingDescriptor
    // ------------------------------------------------------------------

    @Nested
    inner class TableMappingDescriptorTests {

        @Test
        fun shouldSerializeRegexTableMappingToYaml() {
            val desc: TableMappingDescriptor = RegexTableMappingDescriptor(
                pattern = ".*(?<table>[^/]+)\\.csv$",
                tableNameGroup = "table"
            )
            val yaml = yamlMapper.writerFor(TableMappingDescriptor::class.java).writeValueAsString(desc)
            assertTrue(yaml.contains("type: \"regex\"") || yaml.contains("type: regex"), "Should contain type discriminator, got: $yaml")
            assertTrue(yaml.contains("pattern"))
        }

        @Test
        fun shouldDeserializeRegexTableMappingFromYaml() {
            val yaml = """
                type: regex
                pattern: ".*(?<table>[^/]+)\\.csv$"
                tableNameGroup: table
            """.trimIndent()
            val desc = yamlMapper.readValue(yaml, TableMappingDescriptor::class.java)
            assertInstanceOf(RegexTableMappingDescriptor::class.java, desc)
            assertEquals("table", (desc as RegexTableMappingDescriptor).tableNameGroup)
        }

        @Test
        fun shouldDeserializeDirectoryTableMappingFromYaml() {
            val yaml = """
                type: directory
                depth: 2
            """.trimIndent()
            val desc = yamlMapper.readValue(yaml, TableMappingDescriptor::class.java)
            assertInstanceOf(DirectoryTableMappingDescriptor::class.java, desc)
            assertEquals(2, (desc as DirectoryTableMappingDescriptor).depth)
        }

        @Test
        fun shouldDeserializeDirectoryTableMappingWithDefaultDepth() {
            val yaml = """
                type: directory
            """.trimIndent()
            val desc = yamlMapper.readValue(yaml, TableMappingDescriptor::class.java)
            assertInstanceOf(DirectoryTableMappingDescriptor::class.java, desc)
            assertEquals(1, (desc as DirectoryTableMappingDescriptor).depth)
        }
    }

    // ------------------------------------------------------------------
    // ConflictResolution
    // ------------------------------------------------------------------

    @Nested
    inner class ConflictResolutionTests {

        @Test
        fun shouldDeserializeStringShorthand() {
            val yaml = "\"reject\""
            val cr = yamlMapper.readValue(yaml, ConflictResolution::class.java)
            assertEquals(ConflictStrategy.REJECT, cr.default)
            assertTrue(cr.rules.isEmpty())
        }

        @Test
        fun shouldDeserializeUnionShorthand() {
            val yaml = "\"union\""
            val cr = yamlMapper.readValue(yaml, ConflictResolution::class.java)
            assertEquals(ConflictStrategy.UNION, cr.default)
        }

        @Test
        fun shouldDeserializeMapForm() {
            val yaml = """
                default: reject
                orders: union
                customers: reject
            """.trimIndent()
            val cr = yamlMapper.readValue(yaml, ConflictResolution::class.java)
            assertEquals(ConflictStrategy.REJECT, cr.default)
            assertEquals(ConflictStrategy.UNION, cr.strategyFor("orders"))
            assertEquals(ConflictStrategy.REJECT, cr.strategyFor("customers"))
        }

        @Test
        fun shouldFallBackToDefaultForUnknownTable() {
            val yaml = """
                default: union
                orders: reject
            """.trimIndent()
            val cr = yamlMapper.readValue(yaml, ConflictResolution::class.java)
            assertEquals(ConflictStrategy.UNION, cr.strategyFor("unknown_table"))
            assertFalse(cr.hasExplicitRule("unknown_table"))
            assertTrue(cr.hasExplicitRule("orders"))
        }

        @Test
        fun shouldDefaultToRejectWhenNoDefaultSpecified() {
            val yaml = """
                orders: union
            """.trimIndent()
            val cr = yamlMapper.readValue(yaml, ConflictResolution::class.java)
            assertEquals(ConflictStrategy.REJECT, cr.default)
            assertEquals(ConflictStrategy.UNION, cr.strategyFor("orders"))
        }
    }

    // ------------------------------------------------------------------
    // ReaderDescriptor (new table: {mapping, attributes})
    // ------------------------------------------------------------------

    @Nested
    inner class ReaderDescriptorTests {

        @Test
        fun shouldDeserializeReaderWithFormatAndTable() {
            val yaml = """
                type: stub
                label: raw
                format:
                  delimiter: "|"
                table:
                  mapping:
                    type: regex
                    pattern: ".*\\.csv$"
            """.trimIndent()
            val reader = yamlMapper.readValue(yaml, ReaderDescriptor::class.java)
            assertEquals("stub", reader.type)
            assertEquals("raw", reader.label)
            assertInstanceOf(StubFormatDescriptor::class.java, reader.format)
            assertEquals("|", (reader.format as StubFormatDescriptor).delimiter)
            assertNotNull(reader.table)
            assertInstanceOf(RegexTableMappingDescriptor::class.java, reader.table!!.mapping)
        }

        @Test
        fun shouldDeserializeReaderWithoutOptionalFields() {
            val yaml = """
                type: stub
            """.trimIndent()
            val reader = yamlMapper.readValue(yaml, ReaderDescriptor::class.java)
            assertEquals("stub", reader.type)
            assertNull(reader.label)
            assertInstanceOf(StubFormatDescriptor::class.java, reader.format)
            assertNull(reader.table)
        }

        @Test
        fun shouldDeserializeReaderWithEmptyFormat() {
            val yaml = """
                type: stub
                format: {}
            """.trimIndent()
            val reader = yamlMapper.readValue(yaml, ReaderDescriptor::class.java)
            assertInstanceOf(StubFormatDescriptor::class.java, reader.format)
        }

        @Test
        fun shouldDeserializeReaderWithAttributes() {
            val yaml = """
                type: stub
                table:
                  mapping:
                    type: directory
                  attributes:
                    - name: pipeline
                      source: CONSTANT
                      value: "raw-ingest"
                    - name: year
                      source: REGEX
                      pattern: ".*_(?<year>\\d{4})\\.csv$"
                      group: year
                      type: INT
            """.trimIndent()
            val reader = yamlMapper.readValue(yaml, ReaderDescriptor::class.java)
            assertNotNull(reader.table)
            assertEquals(2, reader.table!!.attributes.size)
            assertEquals("pipeline", reader.table!!.attributes[0].name)
            assertEquals(AttributeSource.CONSTANT, reader.table!!.attributes[0].source)
            assertEquals("year", reader.table!!.attributes[1].name)
            assertEquals(AttributeSource.REGEX, reader.table!!.attributes[1].source)
            assertEquals(AttributeType.INT, reader.table!!.attributes[1].type)
        }
    }

    // ------------------------------------------------------------------
    // SourceDescriptor (multi-reader with table: {mapping})
    // ------------------------------------------------------------------

    @Nested
    inner class SourceDescriptorTests {

        @Test
        fun shouldDeserializeMultiReaderSource() {
            val yaml = """
                name: warehouse
                storage:
                  type: local
                  rootPath: /data/warehouse
                table:
                  mapping:
                    type: directory
                    depth: 1
                conflicts: reject
                readers:
                  - type: stub
                    label: raw
                    format:
                      delimiter: ","
                  - type: stub
                    label: processed
                    format:
                      delimiter: "|"
                    table:
                      mapping:
                        type: regex
                        pattern: ".*\\.tsv$"
            """.trimIndent()
            val desc = yamlMapper.readValue(yaml, SourceDescriptor::class.java)
            assertEquals("warehouse", desc.name)
            assertInstanceOf(LocalStorageDescriptor::class.java, desc.storage)
            assertNotNull(desc.table)
            assertInstanceOf(DirectoryTableMappingDescriptor::class.java, desc.table!!.mapping)
            assertEquals(ConflictStrategy.REJECT, desc.conflicts.default)
            assertEquals(2, desc.readers.size)

            val r1 = desc.readers[0]
            assertEquals("stub", r1.type)
            assertEquals("raw", r1.label)
            assertNull(r1.table) // inherits from source level

            val r2 = desc.readers[1]
            assertEquals("processed", r2.label)
            assertNotNull(r2.table)
            assertInstanceOf(RegexTableMappingDescriptor::class.java, r2.table!!.mapping)
        }

        @Test
        fun shouldDeserializeSingleReaderSource() {
            val yaml = """
                name: simple
                storage:
                  type: local
                  rootPath: /data
                readers:
                  - type: stub
                    table:
                      mapping:
                        type: regex
                        pattern: ".*\\.csv$"
            """.trimIndent()
            val desc = yamlMapper.readValue(yaml, SourceDescriptor::class.java)
            assertEquals(1, desc.readers.size)
            assertEquals(ConflictStrategy.REJECT, desc.conflicts.default)
        }

        @Test
        fun shouldDeserializeWithMapConflicts() {
            val yaml = """
                name: mixed
                storage:
                  type: local
                  rootPath: /data
                conflicts:
                  default: reject
                  orders: union
                readers:
                  - type: stub
                    table:
                      mapping:
                        type: directory
            """.trimIndent()
            val desc = yamlMapper.readValue(yaml, SourceDescriptor::class.java)
            assertEquals(ConflictStrategy.REJECT, desc.conflicts.default)
            assertEquals(ConflictStrategy.UNION, desc.conflicts.strategyFor("orders"))
        }

        @Test
        fun shouldDeserializeWithSourceLevelAttributes() {
            val yaml = """
                name: enriched
                storage:
                  type: local
                  rootPath: /data
                table:
                  mapping:
                    type: directory
                  attributes:
                    - name: source_id
                      source: CONSTANT
                      value: "warehouse-01"
                readers:
                  - type: stub
            """.trimIndent()
            val desc = yamlMapper.readValue(yaml, SourceDescriptor::class.java)
            assertNotNull(desc.table)
            assertEquals(1, desc.table!!.attributes.size)
            assertEquals("source_id", desc.table!!.attributes[0].name)
        }

        @Test
        fun shouldDefaultConflictsToReject() {
            val yaml = """
                name: noconflict
                storage:
                  type: local
                  rootPath: /data
                readers:
                  - type: stub
                    table:
                      mapping:
                        type: directory
            """.trimIndent()
            val desc = yamlMapper.readValue(yaml, SourceDescriptor::class.java)
            assertEquals(ConflictStrategy.REJECT, desc.conflicts.default)
            assertTrue(desc.conflicts.rules.isEmpty())
        }
    }

    // ------------------------------------------------------------------
    // TableAttributeDescriptor
    // ------------------------------------------------------------------

    @Nested
    inner class TableAttributeDescriptorTests {

        @Test
        fun shouldDeserializeRegexAttribute() {
            val yaml = """
                name: year
                source: REGEX
                pattern: ".*_(?<year>\\d{4})\\.csv$"
                group: year
                type: INT
            """.trimIndent()
            val attr = yamlMapper.readValue(yaml, TableAttributeDescriptor::class.java)
            assertEquals("year", attr.name)
            assertEquals(AttributeSource.REGEX, attr.source)
            assertEquals(AttributeType.INT, attr.type)
            assertNotNull(attr.pattern)
            assertEquals("year", attr.group)
        }

        @Test
        fun shouldDeserializeConstantAttribute() {
            val yaml = """
                name: pipeline
                source: CONSTANT
                value: raw-ingest
            """.trimIndent()
            val attr = yamlMapper.readValue(yaml, TableAttributeDescriptor::class.java)
            assertEquals("pipeline", attr.name)
            assertEquals(AttributeSource.CONSTANT, attr.source)
            assertEquals("raw-ingest", attr.value)
            assertEquals(AttributeType.STRING, attr.type)
        }

        @Test
        fun shouldDeserializeDateAttributeWithFormat() {
            val yaml = """
                name: file_date
                source: REGEX
                pattern: ".*_(?<date>\\d{8})\\.csv$"
                group: date
                type: DATE
                format: ddMMyyyy
            """.trimIndent()
            val attr = yamlMapper.readValue(yaml, TableAttributeDescriptor::class.java)
            assertEquals(AttributeType.DATE, attr.type)
            assertEquals("ddMMyyyy", attr.format)
        }
    }

    // ------------------------------------------------------------------
    // SPI discovery
    // ------------------------------------------------------------------

    @Nested
    inner class SpiDiscoveryTests {

        @Test
        fun shouldDiscoverCoreSubtypesViaSpi() {
            val providers = java.util.ServiceLoader.load(DescriptorSubtypeProvider::class.java).toList()
            assertTrue(providers.isNotEmpty(), "Should discover at least the core provider")

            val allSubtypes = providers.flatMap { it.subtypes() }
            assertTrue(allSubtypes.any { it.type == LocalStorageDescriptor::class.java })
            assertTrue(allSubtypes.any { it.type == RegexTableMappingDescriptor::class.java })
            assertTrue(allSubtypes.any { it.type == DirectoryTableMappingDescriptor::class.java })
        }

        @Test
        fun shouldIncludeTestSubtypesViaSpi() {
            val providers = java.util.ServiceLoader.load(DescriptorSubtypeProvider::class.java).toList()
            val allSubtypes = providers.flatMap { it.subtypes() }
            assertTrue(allSubtypes.any { it.type == StubFormatDescriptor::class.java },
                "Should discover StubFormatDescriptor from test SPI")
        }
    }

    // ------------------------------------------------------------------
    // DescriptorModule
    // ------------------------------------------------------------------

    @Nested
    inner class DescriptorModuleTests {

        @Test
        fun shouldCreateModuleWithoutError() {
            val module = DescriptorModule()
            assertNotNull(module)
            assertEquals("MillSourceDescriptorModule", module.moduleName)
        }

        @Test
        fun shouldRegisterModuleOnMapper() {
            val mapper = ObjectMapper(YAMLFactory())
                .registerModule(KotlinModule.Builder().build())
                .registerModule(DescriptorModule())
            assertNotNull(mapper)

            val yaml = "type: local\nrootPath: /test"
            val desc = mapper.readValue(yaml, StorageDescriptor::class.java)
            assertInstanceOf(LocalStorageDescriptor::class.java, desc)
        }
    }
}
