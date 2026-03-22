package io.qpointz.mill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrnSlugTest {

    // ── Mode 1: Full slug round-trips ──────────────────────────────────

    @Test
    void shouldEncodeAndDecodeDescriptiveFacetTypeUrn() {
        String urn  = "urn:mill/metadata/facet-type:descriptive";
        String slug = UrnSlug.encode(urn);
        assertFalse(slug.contains("/"), "slug must not contain /");
        assertFalse(slug.startsWith("urn:"), "slug must not start with urn:");
        assertEquals(urn, UrnSlug.decode(slug));
    }

    @Test
    void shouldEncodeAndDecodeStructuralFacetTypeUrn() {
        String urn = "urn:mill/metadata/facet-type:structural";
        assertEquals(urn, UrnSlug.decode(UrnSlug.encode(urn)));
    }

    @Test
    void shouldEncodeAndDecodeRelationFacetTypeUrn() {
        String urn = "urn:mill/metadata/facet-type:relation";
        assertEquals(urn, UrnSlug.decode(UrnSlug.encode(urn)));
    }

    @Test
    void shouldEncodeAndDecodeConceptFacetTypeUrn() {
        String urn = "urn:mill/metadata/facet-type:concept";
        assertEquals(urn, UrnSlug.decode(UrnSlug.encode(urn)));
    }

    @Test
    void shouldEncodeAndDecodeValueMappingFacetTypeUrn() {
        String urn = "urn:mill/metadata/facet-type:value-mapping";
        assertEquals(urn, UrnSlug.decode(UrnSlug.encode(urn)));
    }

    @Test
    void shouldEncodeAndDecodeGlobalScopeUrn() {
        String urn = "urn:mill/metadata/scope:global";
        assertEquals(urn, UrnSlug.decode(UrnSlug.encode(urn)));
    }

    @Test
    void shouldPreserveHyphensInLocalId_whenRoundTripping() {
        String urn  = "urn:mill/metadata/facet-type:value-mapping";
        String slug = UrnSlug.encode(urn);
        // hyphens in "value-mapping" are escaped to "--" so they survive round-trip
        assertTrue(slug.contains("--"), "escaped hyphens should appear as --");
        assertEquals(urn, UrnSlug.decode(slug));
    }

    @Test
    void shouldEncodeAndDecodeEntityTypeSchemaUrn() {
        String urn = "urn:mill/metadata/entity-type:schema";
        assertEquals(urn, UrnSlug.decode(UrnSlug.encode(urn)));
    }

    @Test
    void shouldThrowOnEncode_whenInputIsNotUrn() {
        assertThrows(IllegalArgumentException.class, () -> UrnSlug.encode("not-a-urn"));
    }

    // ── Mode 2: Prefixed slug round-trips ──────────────────────────────

    @Test
    void shouldEncodeAndDecodeGlobalScope_withPrefix() {
        String prefix = "urn:mill/metadata/scope:";
        String urn    = "urn:mill/metadata/scope:global";
        String slug   = UrnSlug.encode(urn, prefix);
        assertEquals("global", slug);
        assertEquals(urn, UrnSlug.decode(slug, prefix));
    }

    @Test
    void shouldEncodeAndDecodeUserScope_withPrefix() {
        String prefix = "urn:mill/metadata/scope:";
        String urn    = "urn:mill/metadata/scope:user:alice";
        String slug   = UrnSlug.encode(urn, prefix);
        assertEquals("user:alice", slug);
        assertEquals(urn, UrnSlug.decode(slug, prefix));
    }

    @Test
    void shouldEncodeAndDecodeTeamScope_withPrefix() {
        String prefix = "urn:mill/metadata/scope:";
        String urn    = "urn:mill/metadata/scope:team:eng";
        assertEquals(urn, UrnSlug.decode(UrnSlug.encode(urn, prefix), prefix));
    }

    @Test
    void shouldThrowOnPrefixedEncode_whenUrnDoesNotMatchPrefix() {
        assertThrows(IllegalArgumentException.class,
                () -> UrnSlug.encode("urn:mill/metadata/facet-type:descriptive",
                                     "urn:mill/metadata/scope:"));
    }

    // ── normalise helper ───────────────────────────────────────────────

    @Test
    void shouldNormalise_whenInputIsFullUrn() {
        String prefix = "urn:mill/metadata/facet-type:";
        String urn    = "urn:mill/metadata/facet-type:descriptive";
        assertEquals(urn, UrnSlug.normalise(urn, prefix, k -> k));
    }

    @Test
    void shouldNormalise_whenInputIsPrefixedSlug() {
        String prefix = "urn:mill/metadata/scope:";
        assertEquals("urn:mill/metadata/scope:global",
                UrnSlug.normalise("global", prefix, k -> k));
    }

    @Test
    void shouldNormalise_whenInputIsLegacyShortKey() {
        String prefix = "urn:mill/metadata/facet-type:";
        assertEquals("urn:mill/metadata/facet-type:descriptive",
                UrnSlug.normalise("descriptive", prefix,
                        k -> "urn:mill/metadata/facet-type:" + k));
    }

    @Test
    void shouldThrowOnNormalise_whenUrnNotInNamespace() {
        String wrongPrefix = "urn:mill/metadata/scope:";
        assertThrows(IllegalArgumentException.class,
                () -> UrnSlug.normalise(
                        "urn:mill/metadata/facet-type:descriptive", wrongPrefix, k -> k));
    }
}
