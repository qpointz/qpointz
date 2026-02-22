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

    // --- GlobTableMapper ---

    @Test
    fun shouldMatchSimpleGlob() {
        val mapper = GlobTableMapper(pattern = "**/*.csv", tableName = "orders")
        val mapping = mapper.mapToTable(blobPath("/data/warehouse/orders.csv"))
        assertNotNull(mapping)
        assertEquals("orders", mapping!!.tableName)
    }

    @Test
    fun shouldReturnNull_whenGlobDoesNotMatch() {
        val mapper = GlobTableMapper(pattern = "**/*.parquet", tableName = "orders")
        val mapping = mapper.mapToTable(blobPath("/data/warehouse/orders.csv"))
        assertNull(mapping)
    }

    @Test
    fun shouldMatchRecursiveGlob() {
        val mapper = GlobTableMapper(pattern = "/data/**/book*.csv", tableName = "books")
        assertNotNull(mapper.mapToTable(blobPath("/data/books/book-001.csv")))
        assertNotNull(mapper.mapToTable(blobPath("/data/archive/2023/book-old.csv")))
        assertNull(mapper.mapToTable(blobPath("/data/archive/2023/orders.csv")))
    }

    @Test
    fun shouldReturnFixedTableName() {
        val mapper = GlobTableMapper(pattern = "**/*.csv", tableName = "my_table")
        assertEquals("my_table", mapper.mapToTable(blobPath("/a.csv"))!!.tableName)
        assertEquals("my_table", mapper.mapToTable(blobPath("/x/y/z.csv"))!!.tableName)
    }

    @Test
    fun shouldMatchSingleDirectoryWildcard() {
        val mapper = GlobTableMapper(pattern = "/data/*/items.csv", tableName = "items")
        assertNotNull(mapper.mapToTable(blobPath("/data/raw/items.csv")))
        assertNull(mapper.mapToTable(blobPath("/data/raw/nested/items.csv")))
    }

    @Test
    fun shouldMatchQuestionMarkWildcard() {
        val mapper = GlobTableMapper(pattern = "/data/file?.csv", tableName = "files")
        assertNotNull(mapper.mapToTable(blobPath("/data/fileA.csv")))
        assertNotNull(mapper.mapToTable(blobPath("/data/file1.csv")))
        assertNull(mapper.mapToTable(blobPath("/data/file12.csv")))
    }

    @Test
    fun shouldMatchBraceAlternatives() {
        val mapper = GlobTableMapper(pattern = "**/*.{csv,tsv}", tableName = "text_data")
        assertNotNull(mapper.mapToTable(blobPath("/data/orders.csv")))
        assertNotNull(mapper.mapToTable(blobPath("/data/orders.tsv")))
        assertNull(mapper.mapToTable(blobPath("/data/orders.parquet")))
    }

    @Test
    fun shouldNotMatchDifferentExtension() {
        val mapper = GlobTableMapper(pattern = "**/*.csv", tableName = "csv_only")
        assertNull(mapper.mapToTable(blobPath("/data/file.tsv")))
        assertNull(mapper.mapToTable(blobPath("/data/file.parquet")))
        assertNull(mapper.mapToTable(blobPath("/data/file.csv.bak")))
    }

    @Test
    fun shouldMatchDeepNestedPaths() {
        val mapper = GlobTableMapper(pattern = "/warehouse/**/sales/**/*.parquet", tableName = "sales")
        assertNotNull(mapper.mapToTable(blobPath("/warehouse/2024/sales/q1/data.parquet")))
        assertNotNull(mapper.mapToTable(blobPath("/warehouse/region/us/sales/monthly/jan.parquet")))
        assertNull(mapper.mapToTable(blobPath("/warehouse/2024/returns/q1/data.parquet")))
    }

    @Test
    fun shouldMatchExactFilename() {
        val mapper = GlobTableMapper(pattern = "**/config.yaml", tableName = "config")
        assertNotNull(mapper.mapToTable(blobPath("/app/config.yaml")))
        assertNotNull(mapper.mapToTable(blobPath("/a/b/c/config.yaml")))
        assertNull(mapper.mapToTable(blobPath("/app/config.yml")))
    }

    @Test
    fun shouldReturnEmptyPartitionValues() {
        val mapper = GlobTableMapper(pattern = "**/*.csv", tableName = "t")
        val mapping = mapper.mapToTable(blobPath("/data/file.csv"))
        assertNotNull(mapping)
        assertTrue(mapping!!.partitionValues.isEmpty())
    }
}
