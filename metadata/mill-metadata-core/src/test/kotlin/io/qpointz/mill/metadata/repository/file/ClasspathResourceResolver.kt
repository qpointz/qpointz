package io.qpointz.mill.metadata.repository.file

class ClasspathResourceResolver : ResourceResolver {
    override fun resolve(locationPattern: String): List<ResolvedResource> {
        var path = locationPattern
        if (path.startsWith("classpath:")) path = path.removePrefix("classpath:")
        if (!path.startsWith("/")) path = "/$path"
        val stream = javaClass.getResourceAsStream(path) ?: return emptyList()
        return listOf(ResolvedResource(locationPattern, stream))
    }
}
