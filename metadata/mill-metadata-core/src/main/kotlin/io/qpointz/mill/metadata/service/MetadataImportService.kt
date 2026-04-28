package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.ImportMode
import io.qpointz.mill.metadata.domain.MetadataExportFormat
import java.io.InputStream

/**
 * Summary of a completed import operation.
 *
 * @property entitiesImported   number of entities saved (inserted or updated)
 * @property facetTypesImported number of custom facet types registered from
 *                              `kind: FacetTypeDefinition` documents in the import stream
 * @property errors             list of non-fatal per-entity error messages accumulated during
 *                              the import; the import continues after each error
 */
data class ImportResult(
    val entitiesImported: Int,
    val facetTypesImported: Int,
    val errors: List<String>
)

/**
 * Service for importing and exporting canonical metadata (multi-document YAML / JSON array).
 *
 * Import accepts only `kind:`-discriminated documents (SPEC §15); legacy `entities:` list
 * envelopes are rejected by the parser.
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
     * Exports persisted metadata in canonical form ([io.qpointz.mill.metadata.io.MetadataYamlSerializer]).
     *
     * Emits, in order: all [io.qpointz.mill.metadata.domain.MetadataScope] rows,
     * all catalog [io.qpointz.mill.metadata.domain.FacetTypeDefinition] rows, then every
     * [io.qpointz.mill.metadata.domain.MetadataEntity] with an embedded `facets` list filtered
     * by [scopeQuery] (facet rows only — scope and definition documents are not narrowed).
     *
     * @param scopeQuery `scope` selection: omitted or blank → global scope facets only;
     *                   comma-separated URNs/slugs → union; `all` or `*` → no facet scope filter
     * @param format YAML multi-document or JSON array of the same documents
     * @return YAML or JSON text suitable for [import] (YAML only on the wire for import today)
     */
    fun export(
        scopeQuery: String? = null,
        format: MetadataExportFormat = MetadataExportFormat.YAML
    ): String
}
