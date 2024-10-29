package io.qpointz.mill.services.sample.controllers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest (classes = {SampleController.class})
@EnableAutoConfiguration
@ActiveProfiles("test-jdbc")
@ComponentScan("io.qpointz.mill")
@Slf4j
class SampleControllerTest {

    private MockMvc mockMvc;

    public SampleControllerTest(@Autowired WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void getSchemas() throws Exception {
        val result = mockMvc.perform(MockMvcRequestBuilders
                .get("/sample/schemas")
                .accept(MediaType.APPLICATION_JSON)
        ).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        //assertEquals(List.of("ts", "metadata"), schemaList.read(result.getResponse().getContentAsString()));
        log.info(result.getResponse().getContentAsString());
    }


}