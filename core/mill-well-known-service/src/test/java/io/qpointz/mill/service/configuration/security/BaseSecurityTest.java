package io.qpointz.mill.service.configuration.security;

import io.qpointz.mill.service.controllers.ServiceController;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebAppConfiguration
@SpringBootTest(classes = {ServiceController.class, })
@ComponentScan(basePackages = {"io.qpointz.mill.service", "io.qpointz.mill.security"})
@Slf4j
abstract class BaseSecurityTest {

    @Getter
    @Setter
    @Autowired
    private WebApplicationContext applicationContext;

    @Getter(lazy = true)
    private final MockMvc mockMvc = createMockMvc();

    private MockMvc createMockMvc() {
        return MockMvcBuilders
                .webAppContextSetup(this.getApplicationContext())
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    protected MockHttpServletResponse getResponse(String url) throws Exception {
        return getMockMvc().perform(MockMvcRequestBuilders
                .get(url)
                .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();
    }

    @Test
    void noAuthMetaRequest() throws Exception {
        val response = getResponse("/.well-known/mill");
        assertEquals(200, response.getStatus());
    }


}
