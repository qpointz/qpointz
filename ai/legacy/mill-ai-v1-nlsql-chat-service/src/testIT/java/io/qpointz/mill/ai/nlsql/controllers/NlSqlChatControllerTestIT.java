package io.qpointz.mill.ai.nlsql.controllers;

import io.qpointz.mill.ai.nlsql.model.pojo.Chat;
import io.qpointz.mill.ai.nlsql.NlSqlChatServiceTestApp;
import io.qpointz.mill.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@Slf4j
@SpringBootTest(classes = {NlSqlChatServiceTestApp.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-moneta-slim-it")
public class NlSqlChatControllerTestIT {

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setupClient() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void trivial() throws Exception {
        webTestClient.get()
                .uri("/api/nl2sql/chats")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @Disabled("Requires live LLM call; flaky under CI/dev environments")
    void createChat() throws Exception {
        val chat = webTestClient.post()
                .uri("/api/nl2sql/chats")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(JsonUtils.defaultJsonMapper().writeValueAsString(
                        new Chat.CreateChatRequest("how many cliients")
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Chat.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(chat);
    }

}
