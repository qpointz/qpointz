package io.qpointz.mill.metadata.domain

import java.time.Instant

/**
 * Authoring support attachment keyed by {@link #targetUrn} — examples, category guidance, etc.
 *
 * <p>Facet type definitions stay free of LLM ergonomics; {@code MetadataContent} holds
 * few-shot payloads and category cookbooks ({@code facet-type-example},
 * {@code facet-type-category}).
 *
 * @property contentUrn stable content row URN ({@code urn:mill/metadata/content:…})
 * @property contentKind discriminator, e.g. {@code facet-type-example}
 * @property targetUrn facet type URN or category URN this content describes
 * @property scopeUrn optional scope filter; {@code null} = platform-global
 * @property title optional display title
 * @property description optional narrative
 * @property contentBody JSON (or other) body as stored text
 * @property mediaType MIME type of {@link #contentBody}
 * @property sortOrder ordering hint within {@code (targetUrn, contentKind)}
 * @property enabled when {@code false}, row is omitted from AI wire joins
 * @property schemaVersion optional body schema version
 * @property uuid stable external id from persistence; {@code null} before save in some flows
 * @property createdAt row creation time
 * @property createdBy actor id or {@code null}
 * @property lastModifiedAt last mutation time
 * @property lastModifiedBy last actor id or {@code null}
 */
data class MetadataContent(
    val contentUrn: String,
    val contentKind: String,
    val targetUrn: String,
    val scopeUrn: String? = null,
    val title: String? = null,
    val description: String? = null,
    val contentBody: String,
    val mediaType: String = MEDIA_TYPE_JSON,
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val schemaVersion: String? = null,
    val uuid: String? = null,
    val createdAt: Instant,
    val createdBy: String?,
    val lastModifiedAt: Instant,
    val lastModifiedBy: String?,
) {
    companion object {
        /** JSON body for facet examples and category guidance. */
        const val MEDIA_TYPE_JSON: String = "application/json"

        /** Few-shot facet payload examples keyed by facet type URN. */
        const val KIND_FACET_TYPE_EXAMPLE: String = "facet-type-example"

        /** Category routing guidance keyed by {@code urn:mill/metadata/facet-type-category:<slug>}. */
        const val KIND_FACET_TYPE_CATEGORY: String = "facet-type-category"
    }
}
