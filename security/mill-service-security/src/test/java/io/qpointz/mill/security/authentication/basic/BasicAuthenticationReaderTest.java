package io.qpointz.mill.security.authentication.basic;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class BasicAuthenticationReaderTest {

    private String getValidHeader(String user, String password) {
        val content = String.format("%s:%s", user, password);
        val token = Base64.getEncoder().encode(content.getBytes());
        val encoded = new String(token);
        return String.format("Basic %s", encoded);
    }

    @Test
    void trivial() {
        val token = getValidHeader("user", "password");
        val reader = io.qpointz.mill.security.authentication.basic.BasicAuthenticationReader.builder()
                .tokenSupplier(()->token)
                .build();
        val c = reader.readAuthentication();
        assertTrue(c instanceof UsernamePasswordAuthenticationToken);
        val utoken = (UsernamePasswordAuthenticationToken) c;
        assertEquals("user", utoken.getPrincipal());
        assertEquals("password", utoken.getCredentials());
    }

    @Test
    void nullWhenNoProlog() {
        val token = Base64.getEncoder().encode("user:password".getBytes());
        val encoded = new String(token);
        val reader = BasicAuthenticationReader.builder()
                .tokenSupplier(()->encoded)
                .build();
        assertNull(reader.readAuthentication());
    }

    @Test
    void nullWhenNullToken() {
        val reader = BasicAuthenticationReader.builder()
                .tokenSupplier(()->null)
                .build();
        assertNull(reader.readAuthentication());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "    "})
    void nullWhenEmpty(String token) {
        val reader = BasicAuthenticationReader.builder()
                .tokenSupplier(()->token)
                .build();
        assertNull(reader.readAuthentication());
    }

    @Test
    void throwOnMalformed() {
        val reader = BasicAuthenticationReader.builder()
                .tokenSupplier(()->"Basic II9900")
                .build();
        assertThrows(AuthenticationException.class, ()->reader.readAuthentication());
    }

    @Test
    void thrownOnIncompleteToken() {
        val token = Base64.getEncoder().encode("user".getBytes());
        val encoded = String.format("Basic %s",new String(token));
        val reader = BasicAuthenticationReader.builder()
                .tokenSupplier(()->encoded)
                .build();
        assertThrows(AuthenticationException.class, ()->reader.readAuthentication());
    }
}