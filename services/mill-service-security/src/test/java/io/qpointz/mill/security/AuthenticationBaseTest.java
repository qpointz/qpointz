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
@ComponentScan(basePackages = {
        "io.qpointz.mill.security",
        "io.qpointz.mill.test.services",
        "io.qpointz.mill.test.security"
})
@Import({TestController.class})
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
}, excludeName = {
        "io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration",
        "io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration",
        "io.qpointz.mill.autoconfigure.data.backend.jdbc.JdbcBackendAutoConfiguration",
        "io.qpointz.mill.autoconfigure.data.backend.calcite.CalciteBackendAutoConfiguration",
        "io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendAutoConfiguration"
})
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
