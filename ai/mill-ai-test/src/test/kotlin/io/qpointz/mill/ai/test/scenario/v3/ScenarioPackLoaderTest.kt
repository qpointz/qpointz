package io.qpointz.mill.ai.test.scenario.v3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ScenarioPackLoaderTest {

    @Test
    fun shouldLoadSamplePackFromClasspath() {
        val pack = ScenarioPackLoader.fromClasspath("scenarios/sample-pack.yml")
        assertThat(pack.name).isEqualTo("sample-pack")
        assertThat(pack.profileId).isEqualTo("hello-world")
        assertThat(pack.parameters.mode).isEqualTo("scripted")
        assertThat(pack.run).hasSize(1)
        assertThat(pack.run[0].ask).isEqualTo("Hi")
        assertThat(pack.run[0].script).hasSize(1)
        assertThat(pack.run[0].script!![0].answer).isEqualTo("Hello")
    }

    @Test
    fun shouldDeriveSlugFromPackName() {
        val pack = ScenarioPack(name = "Data Analysis SQL Emit", profileId = "hello-world")
        assertThat(pack.slug()).isEqualTo("data-analysis-sql-emit")
    }
}
