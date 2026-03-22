package io.qpointz.mill.metadata.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MetadataUrnsTest {

    // ── normaliseFacetTypeKey ─────────────────────────────────────────────────

    @Test
    fun shouldNormaliseFacetTypeKey_whenDescriptiveShortKey() {
        assertEquals(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.normaliseFacetTypeKey("descriptive"))
    }

    @Test
    fun shouldNormaliseFacetTypeKey_whenStructuralShortKey() {
        assertEquals(MetadataUrns.FACET_TYPE_STRUCTURAL, MetadataUrns.normaliseFacetTypeKey("structural"))
    }

    @Test
    fun shouldNormaliseFacetTypeKey_whenRelationShortKey() {
        assertEquals(MetadataUrns.FACET_TYPE_RELATION, MetadataUrns.normaliseFacetTypeKey("relation"))
    }

    @Test
    fun shouldNormaliseFacetTypeKey_whenConceptShortKey() {
        assertEquals(MetadataUrns.FACET_TYPE_CONCEPT, MetadataUrns.normaliseFacetTypeKey("concept"))
    }

    @Test
    fun shouldNormaliseFacetTypeKey_whenValueMappingShortKey() {
        assertEquals(MetadataUrns.FACET_TYPE_VALUE_MAPPING, MetadataUrns.normaliseFacetTypeKey("value-mapping"))
    }

    @Test
    fun shouldNormaliseFacetTypeKey_whenAlreadyUrn() {
        val urn = MetadataUrns.FACET_TYPE_DESCRIPTIVE
        assertEquals(urn, MetadataUrns.normaliseFacetTypeKey(urn))
    }

    @Test
    fun shouldNormaliseFacetTypeKey_whenUnknownShortKeyPassthrough() {
        assertEquals("governance", MetadataUrns.normaliseFacetTypeKey("governance"))
    }

    // ── normaliseScopeKey ─────────────────────────────────────────────────────

    @Test
    fun shouldNormaliseScopeKey_whenGlobal() {
        assertEquals(MetadataUrns.SCOPE_GLOBAL, MetadataUrns.normaliseScopeKey("global"))
    }

    @Test
    fun shouldNormaliseScopeKey_whenUserPrefix() {
        assertEquals("urn:mill/metadata/scope:user:alice", MetadataUrns.normaliseScopeKey("user:alice"))
    }

    @Test
    fun shouldNormaliseScopeKey_whenTeamPrefix() {
        assertEquals("urn:mill/metadata/scope:team:eng", MetadataUrns.normaliseScopeKey("team:eng"))
    }

    @Test
    fun shouldNormaliseScopeKey_whenRolePrefix() {
        assertEquals("urn:mill/metadata/scope:role:admin", MetadataUrns.normaliseScopeKey("role:admin"))
    }

    @Test
    fun shouldNormaliseScopeKey_whenAlreadyUrn() {
        val urn = MetadataUrns.SCOPE_GLOBAL
        assertEquals(urn, MetadataUrns.normaliseScopeKey(urn))
    }

    @Test
    fun shouldNormaliseScopeKey_whenUnknownPassthrough() {
        assertEquals("custom-scope", MetadataUrns.normaliseScopeKey("custom-scope"))
    }

    // ── normaliseFacetTypePath ────────────────────────────────────────────────

    @Test
    fun shouldNormaliseFacetTypePath_whenPrefixedSlugDescriptive() {
        assertEquals(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.normaliseFacetTypePath("descriptive"))
    }

    @Test
    fun shouldNormaliseFacetTypePath_whenPrefixedSlugGovernance() {
        assertEquals("urn:mill/metadata/facet-type:governance", MetadataUrns.normaliseFacetTypePath("governance"))
    }

    @Test
    fun shouldNormaliseFacetTypePath_whenFullUrn() {
        val urn = MetadataUrns.FACET_TYPE_STRUCTURAL
        assertEquals(urn, MetadataUrns.normaliseFacetTypePath(urn))
    }

    // ── normaliseScopePath ────────────────────────────────────────────────────

    @Test
    fun shouldNormaliseScopePath_whenGlobalSlug() {
        assertEquals(MetadataUrns.SCOPE_GLOBAL, MetadataUrns.normaliseScopePath("global"))
    }

    @Test
    fun shouldNormaliseScopePath_whenUserSlug() {
        assertEquals("urn:mill/metadata/scope:user:alice", MetadataUrns.normaliseScopePath("user:alice"))
    }

    @Test
    fun shouldNormaliseScopePath_whenFullUrn() {
        val urn = MetadataUrns.SCOPE_GLOBAL
        assertEquals(urn, MetadataUrns.normaliseScopePath(urn))
    }

    // ── scope builder functions ───────────────────────────────────────────────

    @Test
    fun shouldBuildScopeUser_whenUserIdProvided() {
        assertEquals("urn:mill/metadata/scope:user:alice", MetadataUrns.scopeUser("alice"))
    }

    @Test
    fun shouldBuildScopeTeam_whenTeamNameProvided() {
        assertEquals("urn:mill/metadata/scope:team:platform", MetadataUrns.scopeTeam("platform"))
    }

    @Test
    fun shouldBuildScopeRole_whenRoleNameProvided() {
        assertEquals("urn:mill/metadata/scope:role:admin", MetadataUrns.scopeRole("admin"))
    }
}
