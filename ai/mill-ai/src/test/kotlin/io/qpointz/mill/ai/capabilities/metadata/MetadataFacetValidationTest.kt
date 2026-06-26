package io.qpointz.mill.ai.capabilities.metadata

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MetadataFacetValidationTest {

  @Test
  fun shouldAcceptKebabCaseNameSlug() {
    assertThat(
      MetadataFacetValidation.validatePayloadConventions(
        mapOf("name" to "passenger-id-not-null"),
      ),
    ).isEmpty()
  }

  @Test
  fun shouldRejectUnderscoreNameSlug() {
    assertThat(
      MetadataFacetValidation.validatePayloadConventions(
        mapOf("name" to "passenger_id_not_null"),
      ),
    ).singleElement().asString().contains("kebab-case")
  }

  @Test
  fun shouldRejectUpperCaseNameSlug() {
    assertThat(
      MetadataFacetValidation.validatePayloadConventions(
        mapOf("name" to "Passenger-Id-Not-Null"),
      ),
    ).isNotEmpty()
  }

  @Test
  fun shouldIgnoreMissingNameField() {
    assertThat(
      MetadataFacetValidation.validatePayloadConventions(
        mapOf("description" to "Unique passenger identifier."),
      ),
    ).isEmpty()
  }
}
