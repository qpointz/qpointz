package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.persistence.AgentPersistenceContext
import io.qpointz.mill.ai.profile.HelloWorldAgentProfile
import io.qpointz.mill.ai.runtime.ConversationSession
import io.qpointz.mill.ai.runtime.langchain4j.LangChain4jAgent
import io.qpointz.mill.ai.test.scenario.v3.AskRunItem
import io.qpointz.mill.ai.test.scenario.v3.ScenarioPack
import io.qpointz.mill.ai.test.scenario.v3.ScenarioParameters
import io.qpointz.mill.ai.test.scenario.v3.ScriptStep
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProvidedAgentRunnerTest {

    @Test
    fun shouldIgnoreYamlScriptAndUseProvidedAgent() {
        val agentScript = listOf(ScriptStep(answer = "from-agent"))
        val queue = ScriptQueue(agentScript)
        val model = ScriptedStreamingChatModel(
            queue = queue,
            exhaustionContext = ScriptExhaustionContext(
                profileId = "hello-world",
                turnIndex = 0,
                lastEventType = null,
                scriptStepsTotal = 1,
            ),
            onStepConsumed = { },
        )
        val persistence = AgentPersistenceContext()
        val agent = LangChain4jAgent(
            model = model,
            profile = HelloWorldAgentProfile.profile,
            persistenceContext = persistence,
        )
        val runner = ProvidedAgentRunner(agent, persistence)
        val pack = ScenarioPack(
            name = "provided",
            profileId = "hello-world",
            parameters = ScenarioParameters(mode = "live"),
            run = emptyList(),
        )
        val yamlScript = listOf(ScriptStep(answer = "from-yaml"))

        val outcome = runner.runTurn(
            pack = pack,
            item = AskRunItem(ask = "Say hi", script = yamlScript),
            turnIndex = 0,
            session = ConversationSession(profileId = "hello-world"),
        )

        assertThat(outcome.response).isEqualTo("from-agent")
        assertThat(queue.isEmpty()).isTrue()
    }
}
