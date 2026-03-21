package io.qpointz.mill.persistence.security.jpa.hasher

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NoOpPasswordHasherTest {

    private val hasher = NoOpPasswordHasher()

    @Test
    fun `algorithmId is noop`() {
        assertThat(hasher.algorithmId).isEqualTo("noop")
    }

    @Test
    fun `hash produces noop prefix`() {
        assertThat(hasher.hash("password")).isEqualTo("{noop}password")
    }

    @Test
    fun `hash with empty string`() {
        assertThat(hasher.hash("")).isEqualTo("{noop}")
    }
}
