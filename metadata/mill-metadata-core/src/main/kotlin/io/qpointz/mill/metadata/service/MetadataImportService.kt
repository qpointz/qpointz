package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.ImportMode
import io.qpointz.mill.metadata.domain.MetadataUrns
import java.io.InputStream

/**
 * Summary of a completed import operation.
 *
 * @property entitiesImported   number of entities saved (inserted or updated)
 * @property facetTypesImported number of custom facet types registered from the `facet-types:`
 *                              section of the import document
 * @property errors             list of non-fatal per-entity error messages accumulated during
 *                              the import; the import continues after each error
 */
data class ImportResult(
    val entitiesImported: Int,
    val facetTypesImported: Int,
    val errors: List<String>
)

/**
 * Service for importing and exporting metadata in YAML format.
 *
 * The YAML format supports an `entities:` section and an optional `facet-types:` section.
 * Multiple YAML documents separated by `---` are supported in a single resource.
 *
 * Facet type keys and scope keys in the YAML may use legacy short names (`descriptive`,
 * `global`) or URN notation. Both forms are normalised to URN before persistence.
 *
 * Platform facet types (descriptive, structural, relation, concept, value-mapping) are always
 * known and do not need to appear in `facet-types:`.
 */
interface MetadataImportService {

    /**
     * Imports metadata from an [InputStream] (YAML document or multi-document YAML).
     *
     * The caller is responsible for closing the stream after this method returns.
     *
     * @param inputStream the source stream containing the YAML content
     * @param mode        import mode; [ImportMode.MERGE] preserves existing entities not mentioned
     *                    in the input; [ImportMode.REPLACE] deletes all entities before importing
     * @param actorId     identity of the importing actor; recorded in audit entries
     * @return a summary of the import operation including entity and facet type counts and any
     *         accumulated non-fatal errors
     */
    fun import(
        inputStream: InputStream,
        mode: ImportMode = ImportMode.MERGE,
        actorId: String = "system"
    ): ImportResult

    /**
     * Exports all metadata entities as a YAML document compatible with [import].
     *
     * Output always uses URN-notation keys for facet types and scopes.
     *
     * @param scopeKey export only facets stored under this scope key;
     *                 defaults to [MetadataUrns.SCOPE_GLOBAL]
     * @return YAML string that can be re-imported via [import]
     */
    fun export(scopeKey: String = MetadataUrns.SCOPE_GLOBAL): String
}
