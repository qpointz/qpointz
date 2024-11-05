package io.qpointz.mill.services.configuration.security;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test-jdbc")
@Slf4j
public class HttpServiceNoSecurityTest extends BaseSecurityTest {


    @Test
    void noAuthPingRequest() throws Exception {
        val response = getResponse("/security-test/ping");
        assertEquals(200, response.getStatus());
        log.info(response.getContentAsString());
    }

    @Test
    void noAuthUserRequest() throws Exception {
        val response = getResponse("/security-test/username");
        assertEquals(200, response.getStatus());
        log.info(response.getContentAsString());
        assertEquals("anonymousUser",  response.getContentAsString());
        log.info(response.getContentAsString());
    }

}
