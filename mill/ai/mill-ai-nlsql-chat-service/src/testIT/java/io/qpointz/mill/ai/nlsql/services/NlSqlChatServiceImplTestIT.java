package io.qpointz.mill.ai.nlsql.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qpointz.mill.ai.nlsql.model.UserChatMessage;
import io.qpointz.mill.ai.nlsql.model.pojo.Chat;
import io.qpointz.mill.ai.nlsql.model.pojo.ChatMessage;
import io.qpointz.mill.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(classes = {NlSqlChatServiceImplTestIT.class})
@ComponentScan(basePackages = {"io.qpointz"})
@ActiveProfiles("test-moneta-slim-it")
@EnableAutoConfiguration
@EntityScan(basePackages = {"io.qpointz.mill.ai.nlsql"})
public class NlSqlChatServiceImplTestIT {

    @Autowired
    NlSqlChatServiceImpl service;

    @Test
    void trivia() {
        val chat = service.createChat(new Chat.CreateChatRequest("how many clients?"));
        val messages1 = service.listChatMessages(chat.id());
        assertTrue(messages1.isPresent());
        assertTrue(messages1.get().size()>1);

        val post = service.postChatMessage(chat.id(),
                new Chat.SendChatMessageRequest("count clients by country", Map.of()));
        val messages2 = service.listChatMessages(chat.id());
        assertTrue(messages2.get().size() > messages1.get().size());
    }

    void postQuestion(Chat chat, String query) {
        this.service.postChatMessage(chat.id(), new Chat.SendChatMessageRequest(query, Map.of()));
    }


    void createRoundTripData() throws IOException {
        val chat = service.createChat(new Chat.CreateChatRequest("get list of client"));
        List.of(
                "How many customers?",
                "Count customers by country",
                "Take 10 countries with most clients and present as bar chart",
                "Take 10 countries with most clients and present as pie bar",
                "Enrich model and define concept : Define Active clients - are clients with at least two active non defaulted loans and trading stocks on more than two exchanges",
                "Define: Premium clients are clients in ULTRA and WEALTH customer segments. - first_name in CLIENTS table represents client name field shouldn't be null and have length more than 10 characters",
                "Define relation: Client may have one or more loans . Loans client identified by client_id ",
                "Describe model",
                "Which tables are contains loan information",
                "Which data contains in ACCOUNT tables",
                "What is client_id attribute and in which tables it exists",
                "List clients in Kore",
                "Drop phone and email columns from last query",
                "Now join loan information to result",
                "Now export data to excel format",
                "What snail is drinking to sing"
        ).stream().forEach(k-> {
            try {
                log.info("Question:{}", k);
                postQuestion(chat, k);
                log.info("Fall asleep");
                Thread.currentThread().sleep(3000);
            } catch (Exception ex) {
                log.error("Failed:", ex);
            }
        });

        val messages = service.listChatMessages(chat.id()).get();
        val jsonString = JsonUtils.defaultJsonMapper().writeValueAsString(messages);
        Files.writeString(Paths.get(".roundtrip.json"), jsonString);
    }

}
