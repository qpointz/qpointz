package io.qpointz.mill.source

import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * A [BlobSink] backed by the local filesystem.
 *
 * Creates or overwrites files under [rootPath].
 *
 * @property rootPath the root directory for output files
 */
class LocalBlobSink(val rootPath: Path) : BlobSink {

    private val normalizedRoot: Path = rootPath.toAbsolutePath().normalize()

    init {
        require(Files.exists(normalizedRoot)) { "Root path does not exist: $normalizedRoot" }
        require(Files.isDirectory(normalizedRoot)) { "Root path is not a directory: $normalizedRoot" }
    }

    override fun openOutputStream(path: BlobPath): OutputStream {
        val localPath = resolveLocalPath(path)
        localPath.parent?.let { Files.createDirectories(it) }
        return Files.newOutputStream(
            localPath,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
    }

    override fun close() {
        // No resources to release for local filesystem
    }

    private fun resolveLocalPath(path: BlobPath): Path {
        return when (path) {
            is LocalBlobPath -> path.absolutePath
            else -> Path.of(path.uri)
        }
    }
}
