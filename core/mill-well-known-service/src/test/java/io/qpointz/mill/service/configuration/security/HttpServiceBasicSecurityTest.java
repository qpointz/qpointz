package io.qpointz.mill.service.configuration.security;


import io.qpointz.mill.security.configuration.SecurityConfig;
import io.qpointz.mill.security.configuration.ServicesSecurityConfiguration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ActiveProfiles({"test-jdbc","auth-basic"})
@WebAppConfiguration
@EnableAutoConfiguration
@ComponentScan("io.qpointz.mill")
@SpringBootTest(classes = {HttpServiceBasicSecurityTest.class, SecurityConfig.class, ServicesSecurityConfiguration.class})
@Slf4j
public class HttpServiceBasicSecurityTest extends BaseSecurityTest {

    @Test
    void nonAuthorizedRequest() throws Exception {
        val response = getResponse("/services/security-test/username");
        assertEquals(401, response.getStatus());
    }

    @Test
    void authorizedRequest() throws Exception {
        val template = get("/services/security-test/username")
                .with(user("usr1").password("password"));
        val response = this.getMockMvc()
                .perform(template)
                .andReturn()
                .getResponse();
        assertEquals(200, response.getStatus());
        assertEquals("usr1", response.getContentAsString());
    }

}
