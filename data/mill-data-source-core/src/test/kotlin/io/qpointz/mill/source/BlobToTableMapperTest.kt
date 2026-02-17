package io.qpointz.mill.source

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URI

class BlobToTableMapperTest {

    private fun blobPath(uriPath: String): BlobPath = object : BlobPath {
        override val uri: URI = URI.create("file://$uriPath")
    }

    // --- RegexTableMapper ---

    @Test
    fun shouldExtractTableName_whenRegexMatches() {
        val mapper = RegexTableMapper(Regex(".*?(?<table>[^/]+)\\.[^.]+$"))
        val mapping = mapper.mapToTable(blobPath("/data/airlines/csv/cities.csv"))
        assertNotNull(mapping)
        assertEquals("cities", mapping!!.tableName)
    }

    @Test
    fun shouldReturnNull_whenRegexDoesNotMatch() {
        val mapper = RegexTableMapper(Regex(".*\\.parquet$"))
        val mapping = mapper.mapToTable(blobPath("/data/cities.csv"))
        assertNull(mapping)
    }

    @Test
    fun shouldExtractFromComplexPaths() {
        val mapper = RegexTableMapper(Regex(".*?(?<table>[^/]+)\\.parquet$"))
        val mapping = mapper.mapToTable(blobPath("/data/2023/01/items-2023-01-02.parquet"))
        assertNotNull(mapping)
        assertEquals("items-2023-01-02", mapping!!.tableName)
    }

    @Test
    fun shouldUseCustomGroupName() {
        val mapper = RegexTableMapper(
            pattern = Regex(".*?/(?<tbl>[^/]+)/[^/]+$"),
            tableNameGroup = "tbl"
        )
        val mapping = mapper.mapToTable(blobPath("/data/flights/part-001.parquet"))
        assertNotNull(mapping)
        assertEquals("flights", mapping!!.tableName)
    }

    @Test
    fun shouldReturnNull_whenGroupNotFound() {
        val mapper = RegexTableMapper(
            pattern = Regex(".*\\.csv$"),
            tableNameGroup = "table"
        )
        // Pattern matches but has no named group "table"
        val mapping = mapper.mapToTable(blobPath("/data/cities.csv"))
        assertNull(mapping)
    }

    @Test
    fun shouldExtractWithPartitionedPaths() {
        // Extract just the base table name (e.g. "items" or "person") from
        // paths like /hierarchy/2023/01/items-2023-01-02.parquet
        val mapper = RegexTableMapper(Regex(".*?(?<table>items|person)-[^/]+\\.parquet$"))
        assertEquals("items", mapper.mapToTable(blobPath("/h/2023/01/items-2023-01-02.parquet"))!!.tableName)
        assertEquals("person", mapper.mapToTable(blobPath("/h/2023/01/person-2023-01-02.parquet"))!!.tableName)
    }

    // --- DirectoryTableMapper ---

    @Test
    fun shouldUseParentDirectory_withDepth1() {
        val mapper = DirectoryTableMapper(depth = 1)
        val mapping = mapper.mapToTable(blobPath("/data/cities/part-001.parquet"))
        assertNotNull(mapping)
        assertEquals("cities", mapping!!.tableName)
    }

    @Test
    fun shouldUseGrandparentDirectory_withDepth2() {
        val mapper = DirectoryTableMapper(depth = 2)
        val mapping = mapper.mapToTable(blobPath("/data/airlines/csv/cities.csv"))
        assertNotNull(mapping)
        assertEquals("airlines", mapping!!.tableName)
    }

    @Test
    fun shouldReturnNull_whenPathTooShallow() {
        val mapper = DirectoryTableMapper(depth = 5)
        val mapping = mapper.mapToTable(blobPath("/data/file.csv"))
        assertNull(mapping)
    }

    @Test
    fun shouldThrow_whenDepthIsZero() {
        assertThrows(IllegalArgumentException::class.java) {
            DirectoryTableMapper(depth = 0)
        }
    }

    @Test
    fun shouldThrow_whenDepthIsNegative() {
        assertThrows(IllegalArgumentException::class.java) {
            DirectoryTableMapper(depth = -1)
        }
    }

    @Test
    fun shouldHandleRootLevelFiles_withDepth1() {
        val mapper = DirectoryTableMapper(depth = 1)
        // Path: /file.csv -> segments = ["file.csv"], dirIndex = -1
        val mapping = mapper.mapToTable(blobPath("/file.csv"))
        assertNull(mapping)
    }

    @Test
    fun shouldHandleNestedDirectories() {
        val mapper = DirectoryTableMapper(depth = 1)
        assertEquals("01", mapper.mapToTable(blobPath("/data/2023/01/items.parquet"))!!.tableName)
        assertEquals("02", mapper.mapToTable(blobPath("/data/2023/02/items.parquet"))!!.tableName)
    }
}
