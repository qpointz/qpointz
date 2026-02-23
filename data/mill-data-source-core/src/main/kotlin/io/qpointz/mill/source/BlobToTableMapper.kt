package io.qpointz.mill.source

import java.nio.file.Paths

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
 * - [GlobTableMapper] matches blobs with a glob pattern and assigns a fixed table name
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

/**
 * Maps blobs to a fixed table name when their path matches a glob pattern.
 *
 * Glob matching is performed against the blob's URI path (which always uses
 * forward slashes), making it platform-independent.
 *
 * @property pattern   glob expression (e.g. `&#42;&#42;/&#42;.csv`)
 * @property tableName fixed logical table name assigned to every matching blob
 */
class GlobTableMapper(
    val pattern: String,
    val tableName: String
) : BlobToTableMapper {

    private val regex: Regex = globToRegex(pattern)

    override fun mapToTable(path: BlobPath): TableMapping? {
        val uriPath = path.uri.path ?: return null
        return if (regex.containsMatchIn(uriPath)) {
            TableMapping(tableName)
        } else {
            null
        }
    }

    companion object {
        /**
         * Converts a glob pattern to a [Regex] that matches against URI paths
         * (forward-slash separated). This avoids [java.nio.file.FileSystem.getPathMatcher],
         * which uses platform-specific separators and breaks on Windows.
         *
         * Supported glob syntax:
         * - `**&#47;`  — zero or more directories
         * - `**`   — any characters across separators
         * - `*`    — any characters within a single path segment
         * - `?`    — single character within a segment
         * - `{a,b}` — brace alternatives
         * - `[abc]` — character class (passed through to regex)
         *
         * The resulting regex is anchored to match at a path boundary (`/`) or
         * at the start of the string, and at the end of the string.
         */
        internal fun globToRegex(glob: String): Regex {
            val sb = StringBuilder()
            var i = 0
            while (i < glob.length) {
                val c = glob[i]
                when {
                    // "**/" — match zero or more directory segments (greedy, optional)
                    // e.g. "**/foo.csv" matches both "/foo.csv" and "/a/b/foo.csv"
                    c == '*' && i + 1 < glob.length && glob[i + 1] == '*' -> {
                        if (i + 2 < glob.length && glob[i + 2] == '/') {
                            sb.append("(?:.*/)?")
                            i += 3
                        } else {
                            // standalone "**" — match anything including separators
                            sb.append(".*")
                            i += 2
                        }
                    }
                    // "*" — match any characters except "/" (single segment only)
                    c == '*' -> { sb.append("[^/]*"); i++ }
                    // "?" — match exactly one character except "/"
                    c == '?' -> { sb.append("[^/]"); i++ }
                    // "{a,b,c}" — brace alternatives, each alternative is regex-escaped
                    c == '{' -> {
                        val close = glob.indexOf('}', i)
                        if (close > i) {
                            val alternatives = glob.substring(i + 1, close)
                                .split(',')
                                .joinToString("|") { Regex.escape(it) }
                            sb.append("(?:$alternatives)")
                            i = close + 1
                        } else {
                            sb.append("\\{"); i++
                        }
                    }
                    // "[abc]" — character class, passed through verbatim to regex
                    c == '[' -> {
                        val close = glob.indexOf(']', i)
                        if (close > i) {
                            sb.append(glob.substring(i, close + 1))
                            i = close + 1
                        } else {
                            sb.append("\\["); i++
                        }
                    }
                    // escape regex metacharacters so they match literally
                    c in "\\^$.|+()".toSet() -> { sb.append("\\$c"); i++ }
                    // all other characters pass through as-is
                    else -> { sb.append(c); i++ }
                }
            }
            // anchor: match at a "/" boundary (or start of string) through end of string
            return Regex("(?:^|/)${sb}$")
        }
    }
}
