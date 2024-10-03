package io.qpointz.mill.services.security;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;

class NoopAuthenticationProviderTest {

    @Test
    void authenticatesWithAnyPassword() {
        NoopAuthenticationProvider provider = new NoopAuthenticationProvider();
        val au = provider.authenticate(new UsernamePasswordAuthenticationToken("username", "password"));
        assertNotNull(au);
    }


}