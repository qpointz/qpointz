package io.qpointz.mill.ai.capabilities.metadata

/**
 * Consumer-safe view of a [io.qpointz.mill.metadata.domain.MetadataContent] row for AI tools.
 *
 * @property contentUrn stable content row URN
 * @property contentKind discriminator (e.g. `facet-type-example`)
 * @property targetUrn facet type or category URN this content describes
 * @property scopeUrn optional scope filter; `null` = platform-global
 * @property title optional display title
 * @property description optional narrative
 * @property content parsed JSON body when [mediaType] is JSON; otherwise raw string under `body`
 * @property mediaType MIME type of stored body
 * @property sortOrder ordering hint within `(targetUrn, contentKind)`
 */
data class MetadataContentWire(
  val contentUrn: String,
  val contentKind: String,
  val targetUrn: String,
  val scopeUrn: String? = null,
  val title: String? = null,
  val description: String? = null,
  val content: Any?,
  val mediaType: String,
  val sortOrder: Int = 0,
)
