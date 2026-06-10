package io.qpointz.mill.ai.test.scenario

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.qpointz.mill.ai.test.AgentScenarioResult
import io.qpointz.mill.ai.test.scenario.json.JsonExpectations
import io.qpointz.mill.ai.test.scenario.json.JsonListExpectations
import io.qpointz.mill.ai.test.scenario.text.TextExpectations
import org.junit.jupiter.api.Assertions.assertFalse

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "format",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = JsonExpectations::class,     name = "json"),
    JsonSubTypes.Type(value = JsonListExpectations::class, name = "json-list"),
    JsonSubTypes.Type(value = TextExpectations::class,     name = "text"),
)
interface Expectations {
    fun assert(result: AgentScenarioResult)
}

/** Default: assert that the agent returned a non-blank response. */
class DefaultExpectations : Expectations {
    override fun assert(result: AgentScenarioResult) {
        assertFalse(result.response.isBlank(), "Expected a non-blank agent response")
    }
}
