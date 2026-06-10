package io.qpointz.mill.ai.autoconfigure.config

/**
 * Merges per-profile `vector-store` settings with optional `mill.ai.vector-stores` registry entries.
 */
class VectorStoreConfigMerger(
    private val root: AiConfigurationProperties,
) {

    fun resolve(profile: VectorStoreSettings.Profile): VectorStoreSettings.Effective {
        val backendValue = profile.backend.trim()
        val builtIn = VectorStoreSettings.Backend.fromConfigValue(backendValue)
        val base = if (builtIn != null) {
            VectorStoreSettings.Connection().apply {
                backend = builtIn
            }
        } else {
            root.vectorStores[backendValue]
                ?: error(
                    "mill.ai.data.embedding vector-store.backend='$backendValue' is neither a built-in backend " +
                        "(in-memory, chroma, pgvector) nor a mill.ai.vector-stores['$backendValue'] registry key",
                )
        }
        return VectorStoreSettings.Effective(
            backend = base.backend,
            chroma = mergeChroma(base.chroma, profile.chroma),
            pgvector = mergePgVector(base.pgvector, profile.pgvector),
        )
    }

    private fun mergeChroma(
        base: VectorStoreSettings.Chroma,
        override: VectorStoreSettings.Chroma,
    ): VectorStoreSettings.Chroma {
        val merged = VectorStoreSettings.Chroma()
        merged.baseUrl = override.baseUrl?.takeIf { it.isNotBlank() } ?: base.baseUrl
        merged.apiVersion = override.apiVersion ?: base.apiVersion
        merged.tenantName = override.tenantName?.takeIf { it.isNotBlank() } ?: base.tenantName
        merged.databaseName = override.databaseName?.takeIf { it.isNotBlank() } ?: base.databaseName
        merged.collectionName = override.collectionName?.takeIf { it.isNotBlank() } ?: base.collectionName
        merged.timeout = override.timeout ?: base.timeout
        return merged
    }

    private fun mergePgVector(
        base: VectorStoreSettings.PgVector,
        override: VectorStoreSettings.PgVector,
    ): VectorStoreSettings.PgVector {
        val merged = VectorStoreSettings.PgVector()
        merged.table = override.table?.takeIf { it.isNotBlank() } ?: base.table
        merged.isCreateTable = if (override.table?.isNotBlank() == true) override.isCreateTable else base.isCreateTable
        merged.isUseIndex = override.isUseIndex
        merged.indexListSize = override.indexListSize ?: base.indexListSize
        return merged
    }
}
