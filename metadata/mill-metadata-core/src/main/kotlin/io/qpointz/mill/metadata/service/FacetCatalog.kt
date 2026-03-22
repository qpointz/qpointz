package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.ValidationResult
import java.util.Optional

/**
 * Catalog API for facet type registration, querying, and validation policy.
 *
 * The catalog is the authoritative source for which facet types are known, enabled, and
 * applicable to which entity types.
 */
interface FacetCatalog {

    /**
     * Registers a new [FacetTypeDescriptor] with the catalog.
     *
     * @param descriptor the facet type descriptor to register
     * @throws IllegalArgumentException if the descriptor is invalid or already registered
     */
    fun register(descriptor: FacetTypeDescriptor)

    /**
     * Updates an existing [FacetTypeDescriptor].
     *
     * @param descriptor the updated descriptor; [FacetTypeDescriptor.typeKey] identifies the record
     * @throws IllegalArgumentException if no descriptor with the given key exists
     */
    fun update(descriptor: FacetTypeDescriptor)

    /**
     * Deletes a non-mandatory [FacetTypeDescriptor] by its type key.
     *
     * @param typeKey the facet type key (URN or short key)
     * @throws IllegalArgumentException if the type is mandatory or not found
     */
    fun delete(typeKey: String)

    /**
     * Returns the [FacetTypeDescriptor] for the given type key, if present.
     *
     * @param typeKey the facet type key (URN or short key)
     * @return an [Optional] containing the descriptor, or empty if not found
     */
    fun get(typeKey: String): Optional<FacetTypeDescriptor>

    /**
     * Returns all registered [FacetTypeDescriptor] instances.
     *
     * @return collection of all descriptors
     */
    fun getAll(): Collection<FacetTypeDescriptor>

    /**
     * Returns all enabled [FacetTypeDescriptor] instances.
     *
     * @return collection of enabled descriptors
     */
    fun getEnabled(): Collection<FacetTypeDescriptor>

    /**
     * Returns all mandatory [FacetTypeDescriptor] instances.
     *
     * @return collection of mandatory descriptors
     */
    fun getMandatory(): Collection<FacetTypeDescriptor>

    /**
     * Returns all [FacetTypeDescriptor] instances applicable to the given entity-type string.
     *
     * @param targetType entity-type URN string (e.g. `urn:mill/metadata/entity-type:table`)
     * @return collection of descriptors applicable to [targetType]
     */
    fun getForTargetType(targetType: String): Collection<FacetTypeDescriptor>

    /**
     * Returns whether the given facet type is registered and enabled.
     *
     * @param typeKey the facet type key to check
     * @return `true` if registered and enabled
     */
    fun isAllowed(typeKey: String): Boolean

    /**
     * Returns whether the given facet type is mandatory.
     *
     * @param typeKey the facet type key to check
     * @return `true` if mandatory
     */
    fun isMandatory(typeKey: String): Boolean

    /**
     * Returns whether the given facet type is applicable to the specified entity type.
     *
     * @param typeKey    the facet type key to check
     * @param targetType entity-type URN string
     * @return `true` if applicable
     */
    fun isApplicableTo(typeKey: String, targetType: String): Boolean

    /**
     * Validates facet content against the registered content schema, if any.
     *
     * @param typeKey   the facet type key whose content schema to apply
     * @param facetData the facet payload to validate
     * @return a [ValidationResult] indicating success or failure with error messages
     */
    fun validateFacetContent(typeKey: String, facetData: Any?): ValidationResult

    /**
     * Validates all facets on [entity] against their registered content schemas.
     *
     * @param entity the metadata entity whose facets are to be validated
     * @return a [ValidationResult] with all accumulated errors, if any
     */
    fun validateEntityFacets(entity: MetadataEntity): ValidationResult
}
