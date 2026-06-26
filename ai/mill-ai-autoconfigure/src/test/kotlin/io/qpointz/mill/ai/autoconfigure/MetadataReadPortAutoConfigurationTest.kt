package io.qpointz.mill.ai.autoconfigure

import io.qpointz.mill.ai.capabilities.metadata.EmptyMetadataReadPort
import io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort
import io.qpointz.mill.ai.data.metadata.ServiceMetadataReadPort
import io.qpointz.mill.metadata.domain.FacetType
import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataContent
import io.qpointz.mill.metadata.domain.ValidationResult
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.repository.MetadataContentRepository
import io.qpointz.mill.metadata.service.FacetCatalog
import io.qpointz.mill.metadata.service.FacetService
import io.qpointz.mill.metadata.service.MetadataReadContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class MetadataReadPortAutoConfigurationTest {

    private val runner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                AiV3DataAutoConfiguration::class.java,
                AiV3MetadataReadPortFallbackAutoConfiguration::class.java,
            ),
        )
        .withUserConfiguration(StubMetadataStackConfiguration::class.java)
        .withPropertyValues("mill.ai.enabled=true")

    @Test
    fun `should register ServiceMetadataReadPort when metadata stack beans exist`() {
        runner.run { ctx ->
            assertThat(ctx).hasSingleBean(MetadataReadPort::class.java)
            assertThat(ctx.getBean(MetadataReadPort::class.java))
                .isInstanceOf(ServiceMetadataReadPort::class.java)
                .isNotInstanceOf(EmptyMetadataReadPort::class.java)
        }
    }

    @Configuration
    class StubMetadataStackConfiguration {
        @Bean
        fun facetCatalog(): FacetCatalog = object : FacetCatalog {
            override fun findType(typeKey: String): FacetType? = null
            override fun findDefinition(typeKey: String): FacetTypeDefinition? = null
            override fun listDefinitions(): List<FacetTypeDefinition> = emptyList()
            override fun listTypes(): List<FacetType> = emptyList()
            override fun inspect(typeKey: String, payload: Map<String, Any?>): ValidationResult =
                ValidationResult(valid = true, errors = emptyList())
            override fun resolveCardinality(typeKey: String): FacetTargetCardinality =
                FacetTargetCardinality.MULTIPLE
            override fun registerDefinition(definition: FacetTypeDefinition): FacetTypeDefinition = definition
        }

        @Bean
        fun facetService(): FacetService = object : FacetService {
            override fun resolve(entityId: String, context: MetadataReadContext): List<FacetInstance> = emptyList()
            override fun resolveByType(
                entityId: String,
                facetTypeKey: String,
                context: MetadataReadContext,
            ): List<FacetInstance> = emptyList()
            override fun assign(
                entityId: String,
                facetTypeKey: String,
                scopeKey: String,
                payload: Map<String, Any?>,
                actor: String,
            ): FacetInstance = error("not used in test")
            override fun update(uid: String, payload: Map<String, Any?>, actor: String): FacetInstance =
                error("not used in test")
            override fun unassign(uid: String, actor: String): Boolean = false
            override fun unassignAll(entityId: String, facetTypeKey: String, scopeKey: String, actor: String) = Unit
        }

        @Bean
        fun metadataContentRepository(): MetadataContentRepository = object : MetadataContentRepository {
            override fun findAll(): List<MetadataContent> = emptyList()
            override fun findByContentUrn(contentUrn: String): MetadataContent? = null
            override fun findByTarget(targetUrn: String, contentKind: String?): List<MetadataContent> = emptyList()
            override fun save(content: MetadataContent): MetadataContent = content
        }
    }
}
