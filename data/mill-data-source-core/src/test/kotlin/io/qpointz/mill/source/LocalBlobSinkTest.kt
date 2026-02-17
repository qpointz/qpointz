package io.qpointz.mill.source

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class LocalBlobSinkTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun shouldWriteBytes_toNewBlob() {
        LocalBlobSink(tempDir).use { sink ->
            val blobPath = LocalBlobPath.of(tempDir, tempDir.resolve("test.txt"))
            sink.openOutputStream(blobPath).use { out ->
                out.write("Hello, world!".toByteArray())
            }

            val content = Files.readString(tempDir.resolve("test.txt"))
            assertEquals("Hello, world!", content)
        }
    }

    @Test
    fun shouldOverwriteExistingBlob() {
        val filePath = tempDir.resolve("overwrite.txt")
        Files.writeString(filePath, "old content")

        LocalBlobSink(tempDir).use { sink ->
            val blobPath = LocalBlobPath.of(tempDir, filePath)
            sink.openOutputStream(blobPath).use { out ->
                out.write("new content".toByteArray())
            }
        }

        assertEquals("new content", Files.readString(filePath))
    }

    @Test
    fun shouldCreateParentDirectories() {
        LocalBlobSink(tempDir).use { sink ->
            val nested = tempDir.resolve("sub/dir/data.txt")
            val blobPath = LocalBlobPath.of(tempDir, nested)
            sink.openOutputStream(blobPath).use { out ->
                out.write("nested".toByteArray())
            }

            assertTrue(Files.exists(nested))
            assertEquals("nested", Files.readString(nested))
        }
    }

    @Test
    fun shouldThrow_whenRootPathDoesNotExist() {
        assertThrows<IllegalArgumentException> {
            LocalBlobSink(Paths.get("/nonexistent/path/that/does/not/exist"))
        }
    }

    @Test
    fun shouldThrow_whenRootPathIsAFile() {
        val filePath = tempDir.resolve("file.txt")
        Files.writeString(filePath, "content")
        assertThrows<IllegalArgumentException> {
            LocalBlobSink(filePath)
        }
    }

    @Test
    fun shouldRoundTrip_withLocalBlobSource() {
        val testData = "round-trip test data"

        // Write via BlobSink
        LocalBlobSink(tempDir).use { sink ->
            val blobPath = LocalBlobPath.of(tempDir, tempDir.resolve("roundtrip.dat"))
            sink.openOutputStream(blobPath).use { out ->
                out.write(testData.toByteArray())
            }
        }

        // Read back via BlobSource
        LocalBlobSource(tempDir).use { source ->
            val blob = source.listBlobs().first()
            source.openInputStream(blob).use { input ->
                val content = String(input.readAllBytes())
                assertEquals(testData, content)
            }
        }
    }
}
