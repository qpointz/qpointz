package io.qpointz.mill.ai.test.scenario.text

import io.qpointz.mill.ai.test.scenario.Expectations

interface TextAssert {
}

data class TextExpectations(val asserts: List<TextAssert> ) : Expectations {
}