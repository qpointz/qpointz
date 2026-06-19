package io.qpointz.mill.ai.runtime.langchain4j

import dev.langchain4j.agent.tool.ToolExecutionRequest
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import io.qpointz.mill.ai.capabilities.sqlquery.MockSqlValidationService
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryCapabilityDependency
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryCapabilityProvider
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.SqlValidationService
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.ValidationResult
import io.qpointz.mill.ai.core.capability.CapabilityDependencies
import io.qpointz.mill.ai.core.capability.CapabilityDependencyContainer
import io.qpointz.mill.ai.core.capability.CapabilityRegistry
import io.qpointz.mill.ai.persistence.AgentPersistenceContext
import io.qpointz.mill.ai.persistence.InMemoryArtifactStore
import io.qpointz.mill.ai.persistence.InMemoryConversationStore
import io.qpointz.mill.ai.profile.AgentProfile
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.ai.runtime.ConversationSession
import io.qpointz.mill.ai.runtime.events.AgentEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LangChain4jAgentEmitTest {

    private val sqlProfile = AgentProfile(id = "test-sql", capabilityIds = setOf("sql-query"))

    private class ValidateSqlToolModel : StreamingChatModel {
        override fun chat(request: ChatRequest, handler: StreamingChatResponseHandler) {
            val aiMessage = AiMessage.from(
                listOf(
                    ToolExecutionRequest.builder()
                        .id("call-1")
                        .name("validate_sql")
                        .arguments("""{"sql":"SELECT 1","attempt":1}""")
                        .build(),
                ),
            )
            handler.onCompleteResponse(ChatResponse.builder().aiMessage(aiMessage).build())
        }
    }

    @Test
    fun shouldEmitSingleGeneratedSql_andSqlValidation_onSuccessfulValidateSql() {
        val artifactStore = InMemoryArtifactStore()
        val conversationStore = InMemoryConversationStore()
        val persistenceContext = AgentPersistenceContext(
            conversationStore = conversationStore,
            artifactStore = artifactStore,
        )
        val registry = CapabilityRegistry.from(listOf(SqlQueryCapabilityProvider()))
        val context = AgentContext(
            contextType = "general",
            capabilityDependencies = CapabilityDependencyContainer.of(
                "sql-query" to CapabilityDependencies.of(
                    SqlQueryCapabilityDependency(MockSqlValidationService()),
                ),
            ),
        )
        val events = mutableListOf<AgentEvent>()
        val session = ConversationSession(profileId = sqlProfile.id)
        val agent = LangChain4jAgent(
            model = ValidateSqlToolModel(),
            profile = sqlProfile,
            registry = registry,
            persistenceContext = persistenceContext,
        )

        agent.run("show data", session, context, events::add)

        val protocolFinals = events.filterIsInstance<AgentEvent.ProtocolFinal>()
        assertEquals(1, protocolFinals.size)
        assertEquals("sql-query.generated-sql", protocolFinals.single().protocolId)

        val artifacts = artifactStore.findByConversation(session.conversationId)
        assertEquals(1, artifacts.count { it.kind == "sql.generated" })
        assertEquals(0, artifacts.count { it.kind == "sql.validation" })
    }

    @Test
    fun shouldNotEmitGeneratedSql_whenValidationFails() {
        val failingValidator = SqlValidationService {
            ValidationResult(
                passed = false,
                message = "bad sql",
                normalizedSql = null,
            )
        }
        val events = mutableListOf<AgentEvent>()
        val agent = LangChain4jAgent(
            model = ValidateSqlToolModel(),
            profile = sqlProfile,
            registry = CapabilityRegistry.from(listOf(SqlQueryCapabilityProvider())),
            persistenceContext = AgentPersistenceContext(),
        )
        agent.run(
            "show data",
            ConversationSession(profileId = sqlProfile.id),
            AgentContext(
                contextType = "general",
                capabilityDependencies = CapabilityDependencyContainer.of(
                    "sql-query" to CapabilityDependencies.of(SqlQueryCapabilityDependency(failingValidator)),
                ),
            ),
            events::add,
        )
        assertTrue(events.filterIsInstance<AgentEvent.ProtocolFinal>().isEmpty())
    }
}
