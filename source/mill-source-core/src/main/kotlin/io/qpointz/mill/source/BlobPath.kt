package io.qpointz.mill.source

import java.net.URI

/**
 * Identifies a single "file-alike" resource in any storage system.
 *
 * A [BlobPath] is a lightweight handle — it does not open or read data.
 * To access the content, pass this to a [BlobSource].
 *
 * @see BlobSource
 */
interface BlobPath {
    /** Absolute URI identifying this blob. */
    val uri: URI
}
