package io.qpointz.mill.security.authentication.token;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class EntraIdProfileLoaderTest {


    @Test
    void loadProfile() {
        val jwtToken = System.getenv().getOrDefault("AZ_ENTRAID_TOKEN", "");
        assumeFalse(jwtToken == null || jwtToken.isEmpty() );
        val loader = new EntraIdProfileLoader(jwtToken);
        val profile = loader.getProfile();
        assertNotNull(profile);
        assertTrue(profile.principalName()!=null && !profile.principalName().isEmpty());
        assertTrue(!profile.grantedAuthorities().isEmpty());
    }

}