package io.qpointz.mill.ai.capabilities.concept

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ConceptRefsTest {

    @Test
    fun shouldParseFullConceptUrn() {
        val ref = ConceptRefs.parse("urn:mill/model/concept:vip-passengers")
        assertThat(ref).isEqualTo("urn:mill/model/concept:vip-passengers")
        assertThat(ConceptRefs.slugFromRef(ref)).isEqualTo("vip-passengers")
    }

    @Test
    fun shouldRejectCatalogPathAsConceptRef() {
        assertThrows(IllegalArgumentException::class.java) {
            ConceptRefs.parse("premium.customers")
        }
    }

    @Test
    fun shouldRejectPhysicalEntityUrnAsConceptRef() {
        assertThrows(IllegalArgumentException::class.java) {
            ConceptRefs.parse("urn:mill/model/table:moneta.customers")
        }
    }

    @Test
    fun shouldDeriveSlugFromDisplayName() {
        assertThat(ConceptRefs.slugFromName("VIP Passengers")).isEqualTo("vip-passengers")
        assertThat(ConceptRefs.refFromSlug("premium-customers"))
            .isEqualTo("urn:mill/model/concept:premium-customers")
    }
}
