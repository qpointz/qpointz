package io.qpointz.mill.ai.autoconfigure

import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers
import io.qpointz.mill.ai.capabilities.sqlquery.SqlValidationOutcome
import io.qpointz.mill.ai.capabilities.sqlquery.SqlValidator
import io.qpointz.mill.ai.memory.ChatMemoryStore
import io.qpointz.mill.ai.memory.InMemoryChatMemoryStore
import io.qpointz.mill.ai.memory.LlmMemoryStrategy
import io.qpointz.mill.ai.persistence.ActiveArtifactPointerStore
import io.qpointz.mill.ai.persistence.ArtifactStore
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.persistence.RunEventStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.FilteredClassLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.util.function.Supplier

class MillAiV3AutoConfigurationTest {

    // Exclude JpaChatMemoryStore so @ConditionalOnClass in MillAiV3JpaConfiguration
    // evaluates false, leaving only the in-memory defaults active.
    private val runner = ApplicationContextRunner()
        .withClassLoader(FilteredClassLoader("io.qpointz.mill.persistence.ai.jpa.adapters.JpaChatMemoryStore"))
        .withConfiguration(
            AutoConfigurations.of(
                MillAiV3AutoConfiguration::class.java,
                MillAiV3DataAutoConfiguration::class.java,
                MillAiV3SqlValidatorAutoConfiguration::class.java,
            ),
        )

    @Test
    fun `registers in-memory defaults when no user beans present`() {
        runner.run { ctx ->
            assertThat(ctx).hasSingleBean(ChatMemoryStore::class.java)
            assertThat(ctx).hasSingleBean(LlmMemoryStrategy::class.java)
            assertThat(ctx).hasSingleBean(RunEventStore::class.java)
            assertThat(ctx).hasSingleBean(ConversationStore::class.java)
            assertThat(ctx).hasSingleBean(ArtifactStore::class.java)
            assertThat(ctx).hasSingleBean(ActiveArtifactPointerStore::class.java)
        }
    }

    @Test
    fun `user-defined ChatMemoryStore overrides default`() {
        runner
            .withBean("customMemoryStore", ChatMemoryStore::class.java, {
                InMemoryChatMemoryStore(maxConversations = 5)
            })
            .run { ctx ->
                assertThat(ctx).hasSingleBean(ChatMemoryStore::class.java)
                val bean = ctx.getBean(ChatMemoryStore::class.java) as InMemoryChatMemoryStore
                assertThat(bean.maxConversations).isEqualTo(5)
            }
    }

    @Test
    fun `registers SqlValidationService when SqlValidator bean is present`() {
        runner
            .withBean(
                "testSqlValidator",
                SqlValidator::class.java,
                Supplier {
                    SqlValidator { sql ->
                        SqlValidationOutcome(sql.isNotBlank(), null, sql.trim())
                    }
                },
            )
            .run { ctx ->
                assertThat(ctx).hasSingleBean(SqlQueryToolHandlers.SqlValidationService::class.java)
            }
    }
}
