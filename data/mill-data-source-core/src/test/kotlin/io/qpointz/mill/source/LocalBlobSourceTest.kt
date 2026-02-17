package io.qpointz.mill.source

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class LocalBlobSourceTest {

    private fun partitionedRoot(): Path =
        Paths.get("../../test/datasets/partitioned").toAbsolutePath().normalize()

    private fun csvRoot(): Path =
        Paths.get("../../test/datasets/airlines/csv").toAbsolutePath().normalize()

    @Test
    fun shouldListBlobs_whenDirectoryContainsFiles() {
        LocalBlobSource(partitionedRoot()).use { source ->
            val blobs = source.listBlobs().toList()
            assertTrue(blobs.isNotEmpty(), "Should find files in partitioned directory")
        }
    }

    @Test
    fun shouldReturnOnlyRegularFiles() {
        LocalBlobSource(partitionedRoot()).use { source ->
            val blobs = source.listBlobs().toList()
            blobs.forEach { blob ->
                val path = Path.of(blob.uri)
                assertTrue(Files.isRegularFile(path), "Should only return regular files: ${blob.uri}")
                assertFalse(Files.isDirectory(path), "Should not return directories: ${blob.uri}")
            }
        }
    }

    @Test
    fun shouldReturnAbsoluteUris() {
        LocalBlobSource(partitionedRoot()).use { source ->
            val blobs = source.listBlobs().toList()
            blobs.forEach { blob ->
                assertTrue(blob.uri.isAbsolute, "URI should be absolute: ${blob.uri}")
                assertTrue(blob.uri.toString().startsWith("file:"), "URI should be file scheme: ${blob.uri}")
            }
        }
    }

    @Test
    fun shouldProvideRelativePaths_whenLocalBlobPath() {
        LocalBlobSource(partitionedRoot()).use { source ->
            val blobs = source.listBlobs().toList()
            blobs.forEach { blob ->
                assertTrue(blob is LocalBlobPath, "Should be LocalBlobPath")
                val localBlob = blob as LocalBlobPath
                assertFalse(localBlob.relativePath.isAbsolute, "Relative path should not be absolute")
            }
        }
    }

    @Test
    fun shouldOpenInputStream_forValidBlob() {
        LocalBlobSource(csvRoot()).use { source ->
            val blob = source.listBlobs().first()
            source.openInputStream(blob).use { stream ->
                val bytes = stream.readNBytes(10)
                assertTrue(bytes.isNotEmpty(), "Should read bytes from stream")
            }
        }
    }

    @Test
    fun shouldOpenSeekableChannel_forValidBlob() {
        LocalBlobSource(csvRoot()).use { source ->
            val blob = source.listBlobs().first()
            source.openSeekableChannel(blob).use { channel ->
                assertTrue(channel.isOpen, "Channel should be open")
                assertTrue(channel.size() > 0, "Channel should have data")
            }
        }
    }

    @Test
    fun shouldThrow_whenRootPathDoesNotExist() {
        assertThrows<IllegalArgumentException> {
            LocalBlobSource(Paths.get("/nonexistent/path/that/does/not/exist"))
        }
    }

    @Test
    fun shouldThrow_whenRootPathIsAFile() {
        LocalBlobSource(csvRoot()).use { source ->
            val blob = source.listBlobs().first() as LocalBlobPath
            assertThrows<IllegalArgumentException> {
                LocalBlobSource(blob.absolutePath)
            }
        }
    }

    @Test
    fun shouldDiscoverParquetFiles_inHierarchy() {
        LocalBlobSource(partitionedRoot()).use { source ->
            val blobs = source.listBlobs().toList()
            assertTrue(blobs.any { it.uri.path.endsWith(".parquet") }, "Should find .parquet files")
        }
    }
}
