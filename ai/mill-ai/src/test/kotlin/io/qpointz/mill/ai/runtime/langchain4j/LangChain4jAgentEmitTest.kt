package io.qpointz.mill.ai.runtime.langchain4j

import dev.langchain4j.agent.tool.ToolExecutionRequest
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import io.qpointz.mill.ai.capabilities.metadata.FacetCategoryWire
import io.qpointz.mill.ai.capabilities.metadata.MetadataContentWire
import io.qpointz.mill.ai.capabilities.metadata.MetadataAuthoringCapabilityProvider
import io.qpointz.mill.ai.capabilities.metadata.MetadataCapabilityDependency
import io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort
import io.qpointz.mill.metadata.domain.facet.FacetPayloadField
import io.qpointz.mill.metadata.domain.facet.FacetPayloadSchema
import io.qpointz.mill.metadata.domain.facet.FacetSchemaType
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest
import io.qpointz.mill.ai.capabilities.sqlquery.MockSqlValidationService
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryCapabilityDependency
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryCapabilityProvider
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.SqlValidationService
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.ValidationResult
import io.qpointz.mill.ai.core.capability.CapabilityDependencies
import io.qpointz.mill.ai.core.capability.CapabilityDependencyContainer
import io.qpointz.mill.ai.core.capability.CapabilityRegistry
import io.qpointz.mill.ai.core.artifact.ProtocolFinalBatch
import io.qpointz.mill.ai.persistence.InMemoryArtifactStore
import io.qpointz.mill.ai.persistence.InMemoryConversationStore
import io.qpointz.mill.ai.persistence.AgentPersistenceContext
import io.qpointz.mill.ai.profile.AgentProfile
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.ai.runtime.ConversationSession
import io.qpointz.mill.ai.runtime.events.AgentEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LangChain4jAgentEmitTest {

    private val sqlProfile = AgentProfile(id = "test-sql", capabilityIds = setOf("sql-query"))

    private val metadataProfile = AgentProfile(
        id = "test-metadata",
        capabilityIds = setOf("metadata-authoring"),
    )

    private class ProposeFacetToolModel : StreamingChatModel {
        private var invocations = 0

        override fun chat(request: ChatRequest, handler: StreamingChatResponseHandler) {
            if (++invocations > 1) {
                handler.onCompleteResponse(
                    ChatResponse.builder().aiMessage(AiMessage.from("Captured facet proposal.")).build(),
                )
                return
            }
            val aiMessage = AiMessage.from(
                listOf(
                    ToolExecutionRequest.builder()
                        .id("call-facet")
                        .name("propose_facet_assignment")
                        .arguments(
                            """{"facetTypeKey":"descriptive","metadataEntityId":"sales.customers","payload":{"summary":"VIP"}}""",
                        )
                        .build(),
                ),
            )
            handler.onCompleteResponse(ChatResponse.builder().aiMessage(aiMessage).build())
        }
    }

    private class DualProposeFacetToolModel : StreamingChatModel {
        private var invocations = 0

        override fun chat(request: ChatRequest, handler: StreamingChatResponseHandler) {
            if (++invocations > 1) {
                handler.onCompleteResponse(
                    ChatResponse.builder().aiMessage(AiMessage.from("Captured facet proposals.")).build(),
                )
                return
            }
            val aiMessage = AiMessage.from(
                listOf(
                    ToolExecutionRequest.builder()
                        .id("call-facet-1")
                        .name("propose_facet_assignment")
                        .arguments(
                            """{"facetTypeKey":"descriptive","metadataEntityId":"sales.customers","payload":{"summary":"VIP"}}""",
                        )
                        .build(),
                    ToolExecutionRequest.builder()
                        .id("call-facet-2")
                        .name("propose_facet_assignment")
                        .arguments(
                            """{"facetTypeKey":"descriptive","metadataEntityId":"sales.orders","payload":{"summary":"Orders"}}""",
                        )
                        .build(),
                ),
            )
            handler.onCompleteResponse(ChatResponse.builder().aiMessage(aiMessage).build())
        }
    }

    private class PartialFailProposeFacetToolModel : StreamingChatModel {
        private var invocations = 0

        override fun chat(request: ChatRequest, handler: StreamingChatResponseHandler) {
            if (++invocations > 1) {
                handler.onCompleteResponse(
                    ChatResponse.builder().aiMessage(AiMessage.from("Captured partial facet proposals.")).build(),
                )
                return
            }
            val aiMessage = AiMessage.from(
                listOf(
                    ToolExecutionRequest.builder()
                        .id("call-facet-ok")
                        .name("propose_facet_assignment")
                        .arguments(
                            """{"facetTypeKey":"descriptive","metadataEntityId":"sales.customers","payload":{"summary":"VIP"}}""",
                        )
                        .build(),
                    ToolExecutionRequest.builder()
                        .id("call-facet-bad")
                        .name("propose_facet_assignment")
                        .arguments(
                            """{"facetTypeKey":"unknown-type","metadataEntityId":"sales.orders","payload":{"summary":"x"}}""",
                        )
                        .build(),
                ),
            )
            handler.onCompleteResponse(ChatResponse.builder().aiMessage(aiMessage).build())
        }
    }

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

    @Test
    fun shouldPersistFacetProposal_onMetadataCapturePath() {
        val artifactStore = InMemoryArtifactStore()
        val conversationStore = InMemoryConversationStore()
        val persistenceContext = AgentPersistenceContext(
            conversationStore = conversationStore,
            artifactStore = artifactStore,
        )
        val metadataPort = metadataReadPortForTests()
        val events = mutableListOf<AgentEvent>()
        val session = ConversationSession(conversationId = "conv-facet", profileId = metadataProfile.id)
        val agent = LangChain4jAgent(
            model = ProposeFacetToolModel(),
            profile = metadataProfile,
            registry = CapabilityRegistry.from(listOf(MetadataAuthoringCapabilityProvider())),
            persistenceContext = persistenceContext,
        )
        agent.run(
            "propose descriptive facet",
            session,
            AgentContext(
                contextType = "general",
                capabilityDependencies = CapabilityDependencyContainer.of(
                    "metadata-authoring" to CapabilityDependencies.of(
                        MetadataCapabilityDependency(metadataPort),
                    ),
                ),
            ),
            events::add,
        )

        val protocolFinal = events.filterIsInstance<AgentEvent.ProtocolFinal>().single()
        assertEquals("metadata.faceting.capture", protocolFinal.protocolId)
        assertTrue(ProtocolFinalBatch.isBatchEnvelope(protocolFinal.payload))

        val turn = conversationStore.load(session.conversationId)!!.turns.single { it.role == "assistant" }
        assertEquals(1, turn.artifactIds.size)
        val record = artifactStore.findById(turn.artifactIds.single())!!
        assertEquals("metadata.faceting.capture", record.kind)
        @Suppress("UNCHECKED_CAST")
        val inner = record.payload["payload"] as Map<String, Any?>
        assertEquals("descriptive", inner["facetTypeKey"])
        assertEquals("urn:mill/model/table:sales.customers", inner["metadataEntityId"])
    }

    @Test
    fun shouldEmitBatchProtocolFinal_whenTwoParallelFacetCapturesSucceed() {
        val events = mutableListOf<AgentEvent>()
        val agent = LangChain4jAgent(
            model = DualProposeFacetToolModel(),
            profile = metadataProfile,
            registry = CapabilityRegistry.from(listOf(MetadataAuthoringCapabilityProvider())),
            persistenceContext = AgentPersistenceContext(),
        )
        agent.run(
            "propose two facets",
            ConversationSession(profileId = metadataProfile.id),
            metadataContext(),
            events::add,
        )
        val protocolFinal = events.filterIsInstance<AgentEvent.ProtocolFinal>().single()
        val items = ProtocolFinalBatch.expandItemPayloads(protocolFinal.payload)
        assertEquals(2, items.size)
    }

    @Test
    fun shouldEmitPartialBatch_whenOneParallelCaptureFails() {
        val artifactStore = InMemoryArtifactStore()
        val conversationStore = InMemoryConversationStore()
        val persistenceContext = AgentPersistenceContext(
            conversationStore = conversationStore,
            artifactStore = artifactStore,
        )
        val events = mutableListOf<AgentEvent>()
        val session = ConversationSession(conversationId = "conv-partial", profileId = metadataProfile.id)
        val agent = LangChain4jAgent(
            model = PartialFailProposeFacetToolModel(),
            profile = metadataProfile,
            registry = CapabilityRegistry.from(listOf(MetadataAuthoringCapabilityProvider())),
            persistenceContext = persistenceContext,
        )
        agent.run("propose facets", session, metadataContext(), events::add)

        val protocolFinal = events.filterIsInstance<AgentEvent.ProtocolFinal>().single()
        assertEquals(1, ProtocolFinalBatch.expandItemPayloads(protocolFinal.payload).size)

        val turn = conversationStore.load(session.conversationId)!!.turns.single { it.role == "assistant" }
        assertEquals(1, turn.artifactIds.size)
    }

    private fun metadataContext() = AgentContext(
        contextType = "general",
        capabilityDependencies = CapabilityDependencyContainer.of(
            "metadata-authoring" to CapabilityDependencies.of(
                MetadataCapabilityDependency(metadataReadPortForTests()),
            ),
        ),
    )

    private fun metadataReadPortForTests(): MetadataReadPort = object : MetadataReadPort {
        private val descriptiveFacet = FacetTypeManifest(
            typeKey = "descriptive",
            title = "Descriptive",
            description = "Descriptive facet",
            payload = FacetPayloadSchema(
                type = FacetSchemaType.OBJECT,
                title = "Descriptive payload",
                description = "Summary",
                fields = listOf(
                    FacetPayloadField(
                        name = "summary",
                        required = true,
                        schema = FacetPayloadSchema(
                            type = FacetSchemaType.STRING,
                            title = "Summary",
                            description = "Summary",
                        ),
                    ),
                ),
            ),
        )

        override fun listFacetTypes(): List<FacetTypeManifest> = listOf(descriptiveFacet)

        override fun getFacetType(facetTypeKey: String): FacetTypeManifest? =
            listFacetTypes().firstOrNull { it.typeKey == facetTypeKey }

        override fun listEntityFacets(
            metadataEntityId: String,
            scope: String?,
            context: String?,
            origin: String?,
        ) = emptyList<Map<String, Any?>>()

        override fun listContent(targetUrn: String?, contentKind: String?): List<MetadataContentWire> =
            emptyList()

        override fun getContent(contentUrn: String): MetadataContentWire? = null

        override fun listFacetCategories(): List<FacetCategoryWire> = emptyList()

        override fun validateFacetPayload(
            facetTypeKey: String,
            payload: Map<String, Any?>,
            metadataEntityId: String?,
        ): List<String> =
            if (facetTypeKey == "descriptive") emptyList() else listOf("unknown facet type: $facetTypeKey")
    }
}
