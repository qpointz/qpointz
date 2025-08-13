package io.qpointz.mill.ai.nlsql.controllers;

import io.qpointz.mill.ai.nlsql.model.pojo.Chat;
import io.qpointz.mill.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
@SpringBootTest(classes = {NlSqlChatControllerTestIT.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {"io.qpointz"})
@ActiveProfiles("test-moneta-slim-it")
@EnableAutoConfiguration
@AutoConfigureMockMvc
//@DataJpaTest
@EntityScan(basePackages = {"io.qpointz.mill.ai.nlsql"})
public class NlSqlChatControllerTestIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void trivial() throws Exception {
        val result = mockMvc.perform(
                        get("/api/nl2sql/chats")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
    }

    @Test
    void createChat() throws Exception {
        val result = mockMvc.perform(
                post("/api/nl2sql/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.defaultJsonMapper().writeValueAsString(
                                new Chat.CreateChatRequest("how many cliients")
                        )))
                .andExpect(status().isOk())
                .andReturn();
        val chat = JsonUtils.defaultJsonMapper().readValue(result
                .getResponse()
                .getContentAsString(), Chat.class);

        assertNotNull(chat);
    }

}
