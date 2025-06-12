package io.qpointz.mill.services.configuration.security;


import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ActiveProfiles({"test-jdbc","auth-basic"})
public class HttpServiceBasicSecurityTest extends BaseSecurityTest {

    @Test
    void nonAuthorizedRequest() throws Exception {
        val response = getResponse("/security-test/username");
        assertEquals(401, response.getStatus());
    }

    @Test
    void authorizedRequest() throws Exception {
        val template = get("/security-test/username")
                .with(user("usr1").password("password"));
        val response = this.getMockMvc()
                .perform(template)
                .andReturn()
                .getResponse();
        assertEquals(200, response.getStatus());
        assertEquals("usr1", response.getContentAsString());
    }

}
