package io.qpointz.mill.persistence.metadata.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

/**
 * Runtime/observed facet type row in `metadata_facet_type`.
 *
 * @property facetTypeId   Surrogate primary key.
 * @property typeRes       URN key (former `type_key`), unique per type instance.
 * @property slug         Optional slug for ad-hoc queries
 * @property displayName  Optional display name
 * @property description  Optional text
 * @property source       `DEFINED` or `OBSERVED`
 * @property facetTypeDef Optional link to canonical definition row
 */
@Entity
@Table(name = "metadata_facet_type")
class MetadataFacetTypeInstEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facet_type_id", nullable = false)
    val facetTypeId: Long = 0,

    @Column(name = "type_res", nullable = false, length = 255)
    var typeRes: String,

    @Column(name = "slug", length = 255)
    var slug: String?,

    @Column(name = "display_name", length = 512)
    var displayName: String?,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String?,

    @Column(name = "source", nullable = false, length = 32)
    var source: String = "OBSERVED",

    @ManyToOne(optional = true)
    @JoinColumn(
        name = "facet_type_def_id",
        referencedColumnName = "facet_type_def_id",
        foreignKey = ForeignKey(ConstraintMode.CONSTRAINT)
    )
    var facetTypeDef: MetadataFacetTypeEntity?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,

    @Column(name = "created_by", length = 255)
    var createdBy: String?,

    @Column(name = "updated_by", length = 255)
    var updatedBy: String?
)
