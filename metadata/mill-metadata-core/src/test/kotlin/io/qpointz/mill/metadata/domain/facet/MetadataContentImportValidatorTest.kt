package io.qpointz.mill.metadata.domain.facet

import io.qpointz.mill.metadata.domain.MetadataContent
import io.qpointz.mill.metadata.domain.MetadataUrns
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant

class MetadataContentImportValidatorTest {

    private val now = Instant.EPOCH

    @Test
    fun shouldAcceptCategory_whenSlugMatchesBody() {
        val content = categoryContent(
            targetUrn = MetadataUrns.facetTypeCategory("general"),
            body = """{"category":"general","summary":"x"}""",
        )
        assertThatCode { MetadataContentImportValidator.validateCategoryContent(content) }
            .doesNotThrowAnyException()
    }

    @Test
    fun shouldRejectCategory_whenSlugMismatch() {
        val content = categoryContent(
            targetUrn = MetadataUrns.facetTypeCategory("general"),
            body = """{"category":"relation","summary":"x"}""",
        )
        assertThatThrownBy { MetadataContentImportValidator.validateCategoryContent(content) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun shouldAcceptExample_whenTargetKnownAndPayloadPresent() {
        val target = MetadataUrns.FACET_TYPE_DESCRIPTIVE
        val content = exampleContent(target, """{"payload":{"title":"Orders"}}""")
        assertThatCode {
            MetadataContentImportValidator.validateExampleContent(content, setOf(target))
        }.doesNotThrowAnyException()
    }

    @Test
    fun shouldRejectExample_whenTargetUnknown() {
        val content = exampleContent(
            MetadataUrns.FACET_TYPE_DESCRIPTIVE,
            """{"payload":{"title":"Orders"}}""",
        )
        assertThatThrownBy {
            MetadataContentImportValidator.validateExampleContent(content, emptySet())
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    private fun categoryContent(targetUrn: String, body: String) = MetadataContent(
        contentUrn = "urn:mill/metadata/content:test-category",
        contentKind = MetadataContent.KIND_FACET_TYPE_CATEGORY,
        targetUrn = targetUrn,
        contentBody = body,
        createdAt = now,
        createdBy = "test",
        lastModifiedAt = now,
        lastModifiedBy = "test",
    )

    private fun exampleContent(targetUrn: String, body: String) = MetadataContent(
        contentUrn = "urn:mill/metadata/content:test-example",
        contentKind = MetadataContent.KIND_FACET_TYPE_EXAMPLE,
        targetUrn = targetUrn,
        contentBody = body,
        createdAt = now,
        createdBy = "test",
        lastModifiedAt = now,
        lastModifiedBy = "test",
    )
}
