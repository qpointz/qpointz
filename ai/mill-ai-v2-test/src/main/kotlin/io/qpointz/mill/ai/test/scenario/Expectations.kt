package io.qpointz.mill.ai.test.scenario

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.qpointz.mill.ai.test.scenario.json.JsonListExpectations
import io.qpointz.mill.ai.test.scenario.text.TextExpectations

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "format",
    visible = false)
@JsonSubTypes(
    JsonSubTypes.Type(value = JsonListExpectations::class, name = "json-list"),
    JsonSubTypes.Type(value = TextExpectations::class, name = "text")
)
interface Expectations {
}

class DefaultExpectations: Expectations {

}




