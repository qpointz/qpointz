package io.qpointz.mill.security.authentication.token;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;

class BearerTokenAuthenticationReaderTest {

    private String getValidHeader(String token) {
        val content = String.format("%s", token);
        return String.format("Bearer %s", content);
    }

    @Test
    void trivial() {
        val token = getValidHeader("sample-token");
        val reader = BearerTokenAuthenticationReader.builder()
                .tokenSupplier(()->token)
                .build();
        val c = reader.readAuthentication();
        assertTrue(c instanceof BearerTokenAuthenticationToken);
        val utoken = (BearerTokenAuthenticationToken) c;
        assertEquals("sample-token", utoken.getToken());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "    "})
    void nullWhenEmpty(String token) {
        val reader = BearerTokenAuthenticationReader.builder()
                .tokenSupplier(()->token)
                .build();
        assertNull(reader.readAuthentication());
    }

    @Test
    void nullWhenNullToken() {
        val reader = BearerTokenAuthenticationReader.builder()
                .tokenSupplier(()->null)
                .build();
        assertNull(reader.readAuthentication());
    }

    @Test
    void nullWhenNoProlog() {
        val reader = BearerTokenAuthenticationReader.builder()
                .tokenSupplier(()-> "sample-token")
                .build();
        assertNull(reader.readAuthentication());
    }

}