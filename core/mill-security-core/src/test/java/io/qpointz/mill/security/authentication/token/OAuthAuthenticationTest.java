package io.qpointz.mill.security.authentication.token;

import io.qpointz.mill.security.AuthenticationBaseTest;
import io.qpointz.mill.test.services.TestController;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles({"test-trivial", "local-jdbc"})
public class OAuthAuthenticationTest extends AuthenticationBaseTest {

    @Autowired
    @Getter
    @Setter
    private MockOAuth2Server oAuthServer;

    @Test
    void nonAuthenticatedReturns401() throws Exception {
        val statusCode = this.getRestTemplate()
                .getForEntity(this.getBaseUrl() + "/test-security/auth-info", String.class)
                .getStatusCode()
                .value();
        assertEquals(401, statusCode);
    }


    @Test
    void authenticatedReturns200() throws Exception {
        val token = this.getOAuthServer().issueToken();
        val tk = token.serialize();

        val headers = new HttpHeaders();
        headers.setBearerAuth(tk);
        val requestEntity = new HttpEntity(headers);

        val entity = getRestTemplate().exchange(this.getBaseUrl() + "/test-security/auth-info",
                HttpMethod.GET, requestEntity, TestController.AuthInfo.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        val body = entity.getBody();
        assertEquals(token.getJWTClaimsSet().getSubject(), body.principalName());
    }



}
