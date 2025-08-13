package io.qpointz.mill.test.security;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OAuth2ServerConfuguration implements DisposableBean {

    private MockOAuth2Server server;

    public OAuth2ServerConfuguration() {
        val port = 8989;
        log.info("Starting OAUth server on port {}", port);
        this.server = new MockOAuth2Server();
        server.start(port);
    }

    @Bean
    MockOAuth2Server oauthServerBean() {
        return this.server;
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("Shutting down OAuth server");
        this.server.shutdown();
    }
}
