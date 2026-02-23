package io.qpointz.mill.metadata.repository.file

import java.io.IOException
import java.io.InputStream

@FunctionalInterface
/** Resolves a location pattern into open input streams for metadata resources. */
fun interface ResourceResolver {
    @Throws(IOException::class)
    fun resolve(locationPattern: String): List<ResolvedResource>
}

/** Resolved resource descriptor containing origin name and stream. */
data class ResolvedResource(val name: String, val inputStream: InputStream)
