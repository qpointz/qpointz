package io.qpointz.mill.security.authentication.basic;

import io.qpointz.mill.security.AuthenticationBaseTest;
import io.qpointz.mill.test.services.TestController;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles({"test-trivial", "local-jdbc"})
public class BasicAuthenticationTest extends AuthenticationBaseTest {

    @Test
    void nonAuthenticatedReturns401() {
        val entity = this.getRestTemplate()
                .getForEntity(this.getBaseUrl() + "/test-security/auth-info", String.class);
        val statusCode = entity.getStatusCode()
                .value();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), statusCode);
    }


    @Test
    void authenticatedReturnsBasic200() {
        val entity = this.getRestTemplate()
                .withBasicAuth("usr1", "password")
                .getForEntity(this.getBaseUrl() + "/test-security/auth-info", TestController.AuthInfo.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        val body = entity.getBody();
        assertNotNull(body);
        assertEquals("usr1", body.principalName());
    }

}
