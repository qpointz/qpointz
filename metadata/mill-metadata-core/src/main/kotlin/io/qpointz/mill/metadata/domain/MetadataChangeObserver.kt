package io.qpointz.mill.metadata.domain

import org.slf4j.LoggerFactory
import java.time.Instant

/**
 * Sealed hierarchy of events emitted whenever metadata state changes.
 *
 * Every event carries a mandatory [entityId], [actorId], and [occurredAt] timestamp.
 * Observers receive the most-specific subtype and may pattern-match to extract details.
 */
sealed class MetadataChangeEvent {
    /** Identifier of the entity affected by this event. */
    abstract val entityId: String

    /** Identity of the actor who triggered the change. */
    abstract val actorId: String

    /** Wall-clock time at which the change occurred. */
    abstract val occurredAt: Instant

    /**
     * Emitted when a new entity is inserted into the repository.
     *
     * @param entityId   identifier of the created entity
     * @param actorId    actor who created the entity
     * @param occurredAt timestamp of creation
     * @param entity     the newly created [MetadataEntity]
     */
    data class EntityCreated(
        override val entityId: String,
        override val actorId: String,
        override val occurredAt: Instant,
        val entity: MetadataEntity
    ) : MetadataChangeEvent()

    /**
     * Emitted when an existing entity is replaced or updated.
     *
     * @param entityId   identifier of the updated entity
     * @param actorId    actor who made the change
     * @param occurredAt timestamp of the update
     * @param before     the entity state before the update
     * @param after      the entity state after the update
     */
    data class EntityUpdated(
        override val entityId: String,
        override val actorId: String,
        override val occurredAt: Instant,
        val before: MetadataEntity,
        val after: MetadataEntity
    ) : MetadataChangeEvent()

    /**
     * Emitted when an entity is deleted from the repository.
     *
     * @param entityId   identifier of the deleted entity
     * @param actorId    actor who deleted the entity
     * @param occurredAt timestamp of deletion
     * @param entity     the entity state at the time of deletion
     */
    data class EntityDeleted(
        override val entityId: String,
        override val actorId: String,
        override val occurredAt: Instant,
        val entity: MetadataEntity
    ) : MetadataChangeEvent()

    /**
     * Emitted when a single facet is updated on an entity.
     *
     * @param entityId   identifier of the affected entity
     * @param actorId    actor who made the change
     * @param occurredAt timestamp of the update
     * @param facetType  URN key of the facet type
     * @param scopeKey   URN key of the scope
     * @param before     facet payload before the update, or `null` if it did not exist
     * @param after      facet payload after the update
     */
    data class FacetUpdated(
        override val entityId: String,
        override val actorId: String,
        override val occurredAt: Instant,
        val facetType: String,
        val scopeKey: String,
        val before: Any?,
        val after: Any?
    ) : MetadataChangeEvent()

    /**
     * Emitted when a single facet is removed from an entity.
     *
     * @param entityId   identifier of the affected entity
     * @param actorId    actor who deleted the facet
     * @param occurredAt timestamp of deletion
     * @param facetType  URN key of the facet type
     * @param scopeKey   URN key of the scope
     * @param payload    the facet payload at the time of deletion
     */
    data class FacetDeleted(
        override val entityId: String,
        override val actorId: String,
        override val occurredAt: Instant,
        val facetType: String,
        val scopeKey: String,
        val payload: Any?
    ) : MetadataChangeEvent()

    /**
     * Emitted for each entity processed during a bulk import operation.
     *
     * @param entityId   identifier of the imported entity
     * @param actorId    actor who triggered the import
     * @param occurredAt timestamp of the import
     * @param entity     the entity as imported
     * @param mode       the [ImportMode] used for the import
     */
    data class Imported(
        override val entityId: String,
        override val actorId: String,
        override val occurredAt: Instant,
        val entity: MetadataEntity,
        val mode: ImportMode
    ) : MetadataChangeEvent()
}

/**
 * Primary observer interface for metadata change notifications.
 *
 * Only the chain ([MetadataChangeObserverChain]) or a single delegating bean should implement
 * this interface directly. Leaf observer implementations should implement
 * [MetadataChangeObserverDelegate] instead to avoid circular injection.
 */
fun interface MetadataChangeObserver {
    /**
     * Receives a metadata change event.
     *
     * Implementations **must not throw** — swallow and log failures on error.
     *
     * @param event the metadata change event to handle
     */
    fun onEvent(event: MetadataChangeEvent)
}

/**
 * Marker interface for leaf observer implementations (JPA audit writer, search indexer, etc.).
 *
 * Spring collects all [MetadataChangeObserverDelegate] beans into [MetadataChangeObserverChain].
 * The chain itself is a [MetadataChangeObserver] bean but **not** a [MetadataChangeObserverDelegate],
 * which avoids the circular-dependency problem that would arise if `List<MetadataChangeObserver>`
 * injection included the chain itself.
 */
interface MetadataChangeObserverDelegate : MetadataChangeObserver

/**
 * Composite observer that iterates all registered [MetadataChangeObserverDelegate] beans.
 *
 * Individual delegate failures are caught, logged at WARN level, and swallowed so that a
 * failing observer does not prevent other observers from receiving the event.
 *
 * @param delegates the list of leaf observer implementations to notify
 */
class MetadataChangeObserverChain(
    private val delegates: List<MetadataChangeObserverDelegate>
) : MetadataChangeObserver {

    private val log = LoggerFactory.getLogger(MetadataChangeObserverChain::class.java)

    override fun onEvent(event: MetadataChangeEvent) =
        delegates.forEach {
            runCatching { it.onEvent(event) }
                .onFailure { e ->
                    log.warn("Observer {} failed for {}: {}",
                        it::class.simpleName, event::class.simpleName, e.message)
                }
        }
}

/**
 * No-op singleton observer used when no [MetadataChangeObserverDelegate] beans are registered.
 *
 * Emits nothing and performs no side effects.
 */
object NoOpMetadataChangeObserver : MetadataChangeObserver {
    override fun onEvent(event: MetadataChangeEvent) = Unit
}
