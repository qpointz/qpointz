package io.qpointz.mill.source

/**
 * Maps a [BlobPath] to a logical table name and optional partition values.
 *
 * @property tableName        the logical table this blob belongs to
 * @property partitionValues  optional key-value partition metadata (e.g. from Hive-style paths)
 */
data class TableMapping(
    val tableName: String,
    val partitionValues: Map<String, Any> = emptyMap()
)

/**
 * Strategy interface for deciding how blobs map to logical table names.
 *
 * Different storage layouts need different mappers:
 * - [RegexTableMapper] extracts table names from blob paths via named capture groups
 * - [DirectoryTableMapper] uses parent directory names as table names
 *
 * @see TableMapping
 */
interface BlobToTableMapper {

    /**
     * Maps a [BlobPath] to a [TableMapping], or returns `null` if the
     * blob should be skipped (e.g. hidden files, metadata files).
     *
     * @param path the blob to classify
     * @return a [TableMapping] or `null` to skip
     */
    fun mapToTable(path: BlobPath): TableMapping?
}

/**
 * Maps blobs to tables by applying a regex to the blob's URI path.
 *
 * The regex must contain a named capture group (default: `table`) that
 * extracts the table name from the path.
 *
 * Example pattern for `/data/airlines/csv/cities.csv`:
 * ```
 * Regex(".*?(?<table>[^/]+)\\.[^.]+$")
 * ```
 *
 * @property pattern        regex with a named capture group for the table name
 * @property tableNameGroup name of the capture group (default `"table"`)
 */
class RegexTableMapper(
    val pattern: Regex,
    val tableNameGroup: String = "table"
) : BlobToTableMapper {

    override fun mapToTable(path: BlobPath): TableMapping? {
        val pathStr = path.uri.path ?: return null
        val match = pattern.find(pathStr) ?: return null
        val tableName = try {
            match.groups[tableNameGroup]?.value
        } catch (_: IllegalArgumentException) {
            null
        } ?: return null
        return TableMapping(tableName)
    }
}

/**
 * Maps blobs to tables based on directory structure.
 *
 * The parent directory at the specified [depth] from the file becomes the
 * table name. With depth=1 (default), the immediate parent directory is used.
 *
 * Example with depth=1:
 * - `/data/cities/part-001.parquet` -> table `cities`
 * - `/data/flights/part-001.parquet` -> table `flights`
 *
 * @property depth how many levels up from the file to look (1 = immediate parent)
 */
class DirectoryTableMapper(
    val depth: Int = 1
) : BlobToTableMapper {

    init {
        require(depth >= 1) { "Depth must be >= 1, got $depth" }
    }

    override fun mapToTable(path: BlobPath): TableMapping? {
        val uriPath = path.uri.path ?: return null
        val segments = uriPath.split("/").filter { it.isNotEmpty() }
        // segments: ["data", "cities", "part-001.parquet"]
        // For depth=1, we want the element at index (size - 1 - depth) = second-to-last
        val dirIndex = segments.size - 1 - depth
        if (dirIndex < 0) return null
        return TableMapping(segments[dirIndex])
    }
}
