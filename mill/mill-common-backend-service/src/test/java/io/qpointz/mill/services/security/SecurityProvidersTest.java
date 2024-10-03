package io.qpointz.mill.services.security;

import io.qpointz.mill.services.MillServiceBaseTest;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityProvidersTest extends MillServiceBaseTest {

    @Test
    void trivial(@Autowired PasswordEncoder passwordEncoder) {
        val config = List.of(
                Map.of("type",(Object)"noop")
        );
        val providers = SecurityProviders.createAuthProviders(config, passwordEncoder);
        assertNotNull(providers);
    }

    @Test
    void failsIfNoTypeSpecified(@Autowired PasswordEncoder passwordEncoder) {
        val config = List.of(
                Map.of("missingtype",(Object)"noop")
        );
        assertThrows(IllegalArgumentException.class,
                ()-> SecurityProviders.createAuthProviders(config, passwordEncoder));
    }

    @Test
    void unknownTypeThrows(@Autowired PasswordEncoder passwordEncoder) {
        val config = List.of(
                Map.of("type",(Object)"doesntexiststype")
        );
        assertThrows(IllegalArgumentException.class,
                ()-> SecurityProviders.createAuthProviders(config, passwordEncoder));
    }


    @Test
    void createAllProviders(@Autowired PasswordEncoder passwordEncoder) {
        val passwdFile = FileAuthenticationProviderTest.class.getResource("/fileProviders/passwd.yml").getFile();
        final List<Map<String,Object>> configs = List.of(
                Map.of("type", "noop"),
                Map.of("type", "deny"),
                Map.of("type" , "jwt", "issuer-uri", "https://nowhere.org/keys"),
                Map.of("type", "file", "path", passwdFile)
        );
        assertDoesNotThrow(()-> SecurityProviders.createAuthProviders(configs, passwordEncoder));
    }






}