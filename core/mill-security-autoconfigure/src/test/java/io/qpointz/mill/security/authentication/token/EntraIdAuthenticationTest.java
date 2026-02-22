package io.qpointz.mill.security.authentication.token;

import io.qpointz.mill.security.AuthenticationBaseTest;
import io.qpointz.mill.test.services.TestController;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@ActiveProfiles({"test-trivial", "local-jdbc"})
public class EntraIdAuthenticationTest extends AuthenticationBaseTest {

    @Test
    void nonAuthenticatedReturns401() {
        val jwtToken = System.getenv().getOrDefault("AZ_ENTRAID_TOKEN", "");
        assumeFalse(jwtToken == null || jwtToken.isEmpty() );
        val entity = this.getRestTemplate()
                .getForEntity(this.getBaseUrl() + "/test-security/auth-info", String.class);
        val statusCode = entity.getStatusCode()
                .value();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), statusCode);
    }

    @Test
    void authenticatedReturns200() {
        val entity = requestWithAuthToken(this.getRestTemplate(), this.getBaseUrl());

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        val body = entity.getBody();
        assertNotNull(body);
        assertNotEquals("ANONYMOUS", body.principalName());
    }

    public static ResponseEntity<TestController.AuthInfo> requestWithAuthToken(TestRestTemplate restTemplate, String baseUrl) {
        val jwtToken = System.getenv().getOrDefault("AZ_ENTRAID_TOKEN", "");
        assumeFalse(jwtToken == null || jwtToken.isEmpty() );

        val headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        val requestEntity = new HttpEntity(headers);

        val entity = restTemplate.exchange(baseUrl + "/test-security/auth-info",
                HttpMethod.GET, requestEntity, TestController.AuthInfo.class);
        return entity;
    }

}
