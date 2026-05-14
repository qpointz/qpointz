package io.qpointz.mill.cloud.aws.blob

import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.RequestPayer
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonWritableChannelException
import java.nio.channels.SeekableByteChannel

/**
 * A read-only [SeekableByteChannel] backed by S3 ranged GET requests.
 *
 * Each [read] call issues a `GetObject` with a `Range` header spanning
 * from the current [position] up to at most [READ_AHEAD_BYTES] bytes.
 * The object size is taken from [knownContentLength] when provided, else obtained lazily
 * from a single `HeadObject` call, and cached for the lifetime of the channel.
 *
 * This channel is **not writable** — [write] and [truncate] throw
 * [NonWritableChannelException].
 *
 * The channel does **not** own the [S3Client] lifecycle; the caller
 * (typically [S3BlobSource]) is responsible for closing the client.
 *
 * @param client the S3 client to issue requests against
 * @param bucket the S3 bucket name
 * @param key    the S3 object key
 * @param knownContentLength when non-null and non-negative, used as [size] without calling
 *   `HeadObject` (typically from list metadata)
 * @param requesterPays when `true`, sends `x-amz-request-payer: requester` on head and ranged get
 */
class S3SeekableByteChannel(
    private val client: S3Client,
    private val bucket: String,
    private val key: String,
    private val knownContentLength: Long? = null,
    private val requesterPays: Boolean = false
) : SeekableByteChannel {

    companion object {
        /** Read-ahead buffer size per ranged GET (4 MB). */
        const val READ_AHEAD_BYTES: Long = 4L * 1024 * 1024
    }

    @Volatile
    private var open = true

    @Volatile
    private var pos: Long = 0

    /** Cached object size, populated lazily on first call to [size]. */
    private val cachedSize: Long by lazy {
        val known = knownContentLength
        if (known != null && known >= 0L) {
            known
        } else {
            val headBuilder = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
            if (requesterPays) {
                headBuilder.requestPayer(RequestPayer.REQUESTER)
            }
            val resp = client.headObject(headBuilder.build())
            resp.contentLength()
        }
    }

    /**
     * Reads a sequence of bytes from this channel into the given buffer.
     *
     * Issues a ranged `GetObject` request starting at the current position.
     * Advances the position by the number of bytes actually read.
     *
     * @param dst the buffer into which bytes are to be transferred
     * @return the number of bytes read, or `-1` if the position is at or past end-of-file
     * @throws ClosedChannelException if the channel has been closed
     */
    override fun read(dst: ByteBuffer): Int {
        ensureOpen()
        val totalSize = size()
        if (pos >= totalSize) return -1

        val endExclusive = minOf(pos + minOf(dst.remaining().toLong(), READ_AHEAD_BYTES), totalSize)
        val rangeHeader = "bytes=$pos-${endExclusive - 1}"

        val getBuilder = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .range(rangeHeader)
        if (requesterPays) {
            getBuilder.requestPayer(RequestPayer.REQUESTER)
        }
        val request = getBuilder.build()

        client.getObject(request).use { responseStream ->
            val buf = ByteArray(dst.remaining())
            var totalRead = 0
            while (totalRead < buf.size) {
                val n = responseStream.read(buf, totalRead, buf.size - totalRead)
                if (n < 0) break
                totalRead += n
            }
            dst.put(buf, 0, totalRead)
            pos += totalRead
            return totalRead
        }
    }

    /**
     * Not supported — this channel is read-only.
     *
     * @throws NonWritableChannelException always
     */
    override fun write(src: ByteBuffer): Int {
        throw NonWritableChannelException()
    }

    /**
     * Returns the current byte position in this channel.
     *
     * @return the current position
     * @throws ClosedChannelException if the channel has been closed
     */
    override fun position(): Long {
        ensureOpen()
        return pos
    }

    /**
     * Sets the channel's position.
     *
     * @param newPosition the new position (non-negative)
     * @return this channel
     * @throws ClosedChannelException if the channel has been closed
     * @throws IllegalArgumentException if [newPosition] is negative
     */
    override fun position(newPosition: Long): SeekableByteChannel {
        ensureOpen()
        require(newPosition >= 0) { "Position must be non-negative: $newPosition" }
        pos = newPosition
        return this
    }

    /**
     * Returns the size of the S3 object in bytes.
     *
     * The value is taken from list metadata when available, else via `HeadObject` on first access,
     * and cached.
     *
     * @return object size in bytes
     * @throws ClosedChannelException if the channel has been closed
     */
    override fun size(): Long {
        ensureOpen()
        return cachedSize
    }

    /**
     * Not supported — this channel is read-only.
     *
     * @throws NonWritableChannelException always
     */
    override fun truncate(size: Long): SeekableByteChannel {
        throw NonWritableChannelException()
    }

    /**
     * Tells whether this channel is open.
     *
     * @return `true` if the channel has not been closed
     */
    override fun isOpen(): Boolean = open

    /**
     * Closes this channel.
     *
     * This is a no-op regarding S3 resources — the [S3Client] lifecycle is
     * managed by [S3BlobSource].
     */
    override fun close() {
        open = false
    }

    private fun ensureOpen() {
        if (!open) throw ClosedChannelException()
    }
}
