package io.qpointz.mill.source

import java.io.InputStream
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * A [BlobPath] pointing to a file on the local filesystem.
 *
 * @property uri          absolute file URI
 * @property relativePath path relative to the [LocalBlobSource] root
 * @property absolutePath resolved absolute path on disk
 */
data class LocalBlobPath(
    override val uri: URI,
    val relativePath: Path,
    val absolutePath: Path
) : BlobPath {

    companion object {
        /**
         * Creates a [LocalBlobPath] from a [rootPath] and an [absolutePath].
         */
        fun of(rootPath: Path, absolutePath: Path): LocalBlobPath {
            val normalizedRoot = rootPath.toAbsolutePath().normalize()
            val normalizedAbs = absolutePath.toAbsolutePath().normalize()
            val relative = normalizedRoot.relativize(normalizedAbs)
            return LocalBlobPath(
                uri = normalizedAbs.toUri(),
                relativePath = relative,
                absolutePath = normalizedAbs
            )
        }
    }
}

/**
 * A [BlobSource] backed by the local filesystem.
 *
 * Recursively discovers all regular files under [rootPath].
 *
 * @property rootPath the root directory to scan
 */
class LocalBlobSource(val rootPath: Path) : BlobSource {

    private val normalizedRoot: Path = rootPath.toAbsolutePath().normalize()

    init {
        require(Files.exists(normalizedRoot)) { "Root path does not exist: $normalizedRoot" }
        require(Files.isDirectory(normalizedRoot)) { "Root path is not a directory: $normalizedRoot" }
    }

    override fun listBlobs(): Sequence<BlobPath> {
        return Files.walk(normalizedRoot)
            .filter { Files.isRegularFile(it) }
            .map { path -> LocalBlobPath.of(normalizedRoot, path) as BlobPath }
            .iterator()
            .asSequence()
    }

    override fun openInputStream(path: BlobPath): InputStream {
        val localPath = resolveLocalPath(path)
        return Files.newInputStream(localPath, StandardOpenOption.READ)
    }

    override fun openSeekableChannel(path: BlobPath): SeekableByteChannel {
        val localPath = resolveLocalPath(path)
        return Files.newByteChannel(localPath, StandardOpenOption.READ)
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
