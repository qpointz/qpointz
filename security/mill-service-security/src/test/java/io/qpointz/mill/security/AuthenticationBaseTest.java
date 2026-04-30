package io.qpointz.mill.security;

import io.qpointz.mill.test.services.TestController;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = AuthenticationBaseTest.TestApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public abstract class AuthenticationBaseTest {

    /**
     * Concrete Boot anchor for integration-style security tests. {@link AuthenticationBaseTest} stays
     * abstract so subclasses can share wiring; Spring Boot 4 must not load an abstract class via
     * {@link SpringBootTest#classes()}.
     */
    @SpringBootApplication(scanBasePackages = {
            "io.qpointz.mill.security",
            "io.qpointz.mill.test.services",
            "io.qpointz.mill.test.security"
    }, excludeName = {
            "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
            "io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration",
            "io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration",
            "io.qpointz.mill.autoconfigure.data.backend.jdbc.JdbcBackendAutoConfiguration",
            "io.qpointz.mill.autoconfigure.data.backend.calcite.CalciteBackendAutoConfiguration",
            "io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendAutoConfiguration"
    })
    @Import({TestController.class})
    static class TestApplication {
    }

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
