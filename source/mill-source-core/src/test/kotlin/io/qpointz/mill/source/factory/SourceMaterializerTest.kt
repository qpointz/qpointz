package io.qpointz.mill.source.factory

import io.qpointz.mill.source.*
import io.qpointz.mill.source.descriptor.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Paths

class SourceMaterializerTest {

    private val materializer = SourceMaterializer()
    private val csvRoot = Paths.get("../../test/datasets/airlines/csv").toAbsolutePath().normalize()

    // ------------------------------------------------------------------
    // StorageFactory SPI
    // ------------------------------------------------------------------

    @Nested
    inner class StorageFactoryTests {

        @Test
        fun shouldCreateLocalBlobSource() {
            val descriptor = LocalStorageDescriptor(rootPath = csvRoot.toString())
            val blobSource = materializer.createBlobSource(descriptor)
            assertInstanceOf(LocalBlobSource::class.java, blobSource)
            blobSource.close()
        }

        @Test
        fun shouldThrowForUnknownStorageDescriptor() {
            val unknown = object : StorageDescriptor {}
            val ex = assertThrows<IllegalArgumentException> {
                materializer.createBlobSource(unknown)
            }
            assertTrue(ex.message!!.contains("No StorageFactory registered"))
        }
    }

    // ------------------------------------------------------------------
    // TableMapperFactory SPI
    // ------------------------------------------------------------------

    @Nested
    inner class TableMapperFactoryTests {

        @Test
        fun shouldCreateRegexTableMapper() {
            val descriptor = RegexTableMappingDescriptor(
                pattern = ".*(?<table>[^/]+)\\.csv$"
            )
            val mapper = materializer.createTableMapper(descriptor)
            assertInstanceOf(RegexTableMapper::class.java, mapper)
        }

        @Test
        fun shouldCreateDirectoryTableMapper() {
            val descriptor = DirectoryTableMappingDescriptor(depth = 2)
            val mapper = materializer.createTableMapper(descriptor)
            assertInstanceOf(DirectoryTableMapper::class.java, mapper)
            assertEquals(2, (mapper as DirectoryTableMapper).depth)
        }

        @Test
        fun shouldThrowForUnknownTableMappingDescriptor() {
            val unknown = object : TableMappingDescriptor {}
            val ex = assertThrows<IllegalArgumentException> {
                materializer.createTableMapper(unknown)
            }
            assertTrue(ex.message!!.contains("No TableMapperFactory registered"))
        }
    }

    // ------------------------------------------------------------------
    // FormatHandlerFactory SPI (test stub)
    // ------------------------------------------------------------------

    @Nested
    inner class FormatHandlerFactoryTests {

        @Test
        fun shouldCreateStubFormatHandler() {
            val descriptor = StubFormatDescriptor(delimiter = "|")
            val handler = materializer.createFormatHandler(descriptor)
            assertInstanceOf(StubFormatHandler::class.java, handler)
        }

        @Test
        fun shouldThrowForUnknownFormatDescriptor() {
            val unknown = object : FormatDescriptor {}
            val ex = assertThrows<IllegalArgumentException> {
                materializer.createFormatHandler(unknown)
            }
            assertTrue(ex.message!!.contains("No FormatHandlerFactory registered"))
        }
    }

    // ------------------------------------------------------------------
    // Materialize reader
    // ------------------------------------------------------------------

    @Nested
    inner class MaterializeReaderTests {

        @Test
        fun shouldMaterializeReaderWithOwnTable() {
            val reader = ReaderDescriptor(
                type = "stub",
                label = "raw",
                format = StubFormatDescriptor(),
                table = TableDescriptor(
                    mapping = RegexTableMappingDescriptor(pattern = ".*\\.csv$")
                )
            )
            val materialized = materializer.materializeReader(reader, null)
            assertEquals("stub", materialized.type)
            assertEquals("raw", materialized.label)
            assertInstanceOf(StubFormatHandler::class.java, materialized.formatHandler)
            assertInstanceOf(RegexTableMapper::class.java, materialized.tableMapper)
            assertNull(materialized.attributeExtractor)
        }

        @Test
        fun shouldFallBackToDefaultTable() {
            val reader = ReaderDescriptor(
                type = "stub",
                format = StubFormatDescriptor(),
                table = null
            )
            val defaultTable = TableDescriptor(
                mapping = DirectoryTableMappingDescriptor(depth = 1)
            )
            val materialized = materializer.materializeReader(reader, defaultTable)
            assertInstanceOf(DirectoryTableMapper::class.java, materialized.tableMapper)
        }

        @Test
        fun shouldThrowWhenNoMappingAvailable() {
            val reader = ReaderDescriptor(
                type = "stub",
                format = StubFormatDescriptor(),
                table = null
            )
            val ex = assertThrows<IllegalArgumentException> {
                materializer.materializeReader(reader, null)
            }
            assertTrue(ex.message!!.contains("no table mapping"))
        }

        @Test
        fun shouldMaterializeReaderWithAttributes() {
            val reader = ReaderDescriptor(
                type = "stub",
                format = StubFormatDescriptor(),
                table = TableDescriptor(
                    mapping = DirectoryTableMappingDescriptor(),
                    attributes = listOf(
                        TableAttributeDescriptor(
                            name = "pipeline",
                            source = AttributeSource.CONSTANT,
                            value = "raw"
                        )
                    )
                )
            )
            val materialized = materializer.materializeReader(reader, null)
            assertNotNull(materialized.attributeExtractor)
            assertEquals(1, materialized.attributeExtractor!!.attributes.size)
        }

        @Test
        fun shouldUseReaderTableOverSourceTable() {
            val sourceTable = TableDescriptor(
                mapping = DirectoryTableMappingDescriptor(depth = 1),
                attributes = listOf(
                    TableAttributeDescriptor(name = "source_attr", source = AttributeSource.CONSTANT, value = "src")
                )
            )
            val readerTable = TableDescriptor(
                mapping = RegexTableMappingDescriptor(pattern = ".*(?<table>[^/]+)\\.csv$"),
                attributes = listOf(
                    TableAttributeDescriptor(name = "reader_attr", source = AttributeSource.CONSTANT, value = "rdr")
                )
            )
            val reader = ReaderDescriptor(
                type = "stub",
                format = StubFormatDescriptor(),
                table = readerTable
            )
            val materialized = materializer.materializeReader(reader, sourceTable)
            // Reader-level table should win entirely
            assertInstanceOf(RegexTableMapper::class.java, materialized.tableMapper)
            assertNotNull(materialized.attributeExtractor)
            assertEquals("reader_attr", materialized.attributeExtractor!!.attributes[0].name)
        }
    }

    // ------------------------------------------------------------------
    // Full materialization
    // ------------------------------------------------------------------

    @Nested
    inner class MaterializeTests {

        @Test
        fun shouldMaterializeSingleReaderDescriptor() {
            val descriptor = SourceDescriptor(
                name = "test-airlines",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(
                        type = "stub",
                        format = StubFormatDescriptor(delimiter = ","),
                        table = TableDescriptor(
                            mapping = RegexTableMappingDescriptor(
                                pattern = ".*(?<table>[^/]+)\\.csv$"
                            )
                        )
                    )
                )
            )
            val materialized = materializer.materialize(descriptor)

            assertEquals("test-airlines", materialized.name)
            assertInstanceOf(LocalBlobSource::class.java, materialized.blobSource)
            assertEquals(1, materialized.readers.size)
            assertEquals(ConflictStrategy.REJECT, materialized.conflicts.default)

            materialized.close()
        }

        @Test
        fun shouldMaterializeMultiReaderDescriptor() {
            val descriptor = SourceDescriptor(
                name = "multi",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                table = TableDescriptor(
                    mapping = DirectoryTableMappingDescriptor(depth = 1)
                ),
                readers = listOf(
                    ReaderDescriptor(type = "stub", label = "a", format = StubFormatDescriptor()),
                    ReaderDescriptor(type = "stub", label = "b", format = StubFormatDescriptor())
                )
            )
            val materialized = materializer.materialize(descriptor)

            assertEquals(2, materialized.readers.size)
            assertEquals("a", materialized.readers[0].label)
            assertEquals("b", materialized.readers[1].label)

            materialized.close()
        }

        @Test
        fun shouldBeAutoCloseable() {
            val descriptor = SourceDescriptor(
                name = "closeable-test",
                storage = LocalStorageDescriptor(rootPath = csvRoot.toString()),
                readers = listOf(
                    ReaderDescriptor(
                        type = "stub",
                        format = StubFormatDescriptor(),
                        table = TableDescriptor(
                            mapping = DirectoryTableMappingDescriptor(depth = 1)
                        )
                    )
                )
            )
            materializer.materialize(descriptor).use { materialized ->
                assertNotNull(materialized.blobSource)
            }
        }
    }
}
