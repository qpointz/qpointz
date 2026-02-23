package io.qpointz.mill.security;

import io.qpointz.mill.test.services.TestController;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = {AuthenticationBaseTest.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {"io.qpointz"})
@Import({TestController.class})
@EnableAutoConfiguration
public abstract class AuthenticationBaseTest {

    @Autowired
    @Getter
    @Setter
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    protected String getBaseUrl() {
        return String.format("http://localhost:%s" , port);
    }


}
