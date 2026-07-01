package io.qpointz.mill.ai.capabilities.metadata

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MetadataEntityIdsTest {

  @Test
  fun shouldResolveLegacyTableKey() {
    val resolved = MetadataEntityIds.resolveEntity("skymill.passenger")
    assertThat(resolved.catalogPath).isEqualTo("skymill.passenger")
    assertThat(resolved.metadataEntityUrn).isEqualTo("urn:mill/model/table:skymill.passenger")
    assertThat(resolved.entityKind).isEqualTo("table")
  }

  @Test
  fun shouldResolveLegacyColumnKey() {
    val resolved = MetadataEntityIds.resolveEntity("skymill.passenger.id")
    assertThat(resolved.catalogPath).isEqualTo("skymill.passenger.id")
    assertThat(resolved.metadataEntityUrn).isEqualTo("urn:mill/model/attribute:skymill.passenger.id")
  }

  @Test
  fun shouldRebuildUrnFromCanonicalModelUrn() {
    val resolved = MetadataEntityIds.resolveEntity("urn:mill/model/table:skymill.passenger")
    assertThat(resolved.catalogPath).isEqualTo("skymill.passenger")
    assertThat(resolved.metadataEntityUrn).isEqualTo("urn:mill/model/table:skymill.passenger")
  }

  @Test
  fun shouldResolveModelRootCatalogAlias() {
    val resolved = MetadataEntityIds.resolveEntity("model-entity")
    assertThat(resolved.catalogPath).isEqualTo("model-entity")
    assertThat(resolved.metadataEntityUrn).isEqualTo("urn:mill/model/model:model-entity")
    assertThat(resolved.entityKind).isEqualTo("model")
  }

  @Test
  fun shouldRejectConceptUrn() {
    assertThrows<IllegalArgumentException> {
      MetadataEntityIds.resolveEntity("urn:mill/model/concept:vip-passengers")
    }
  }

  @Test
  fun shouldRejectMalformedSlashUrn() {
    assertThrows<IllegalArgumentException> {
      MetadataEntityIds.resolveEntity("urn:mill/model/attribute:skymill/passenger/id")
    }
  }
}
