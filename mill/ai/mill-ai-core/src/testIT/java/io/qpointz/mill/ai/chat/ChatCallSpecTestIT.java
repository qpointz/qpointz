package io.qpointz.mill.ai.chat;

import io.qpointz.mill.ai.BaseIntegrationTestIT;
import io.qpointz.mill.ai.chat.messages.MessageList;
import io.qpointz.mill.ai.chat.messages.specs.TemplateMessageSpec;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static io.qpointz.mill.ai.chat.ChatClientBuilders.defaultBuilder;
import static io.qpointz.mill.ai.chat.messages.MessageTemplates.staticTemplate;
import static io.qpointz.mill.ai.chat.messages.MessageTemplates.text;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {BaseIntegrationTestIT.class})
@ActiveProfiles("test-moneta-slim-it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ChatCallSpecTestIT extends BaseIntegrationTestIT {

    @Autowired
    ChatClient.Builder chatBuilder;

    @Test
    void trivialCall() {
        val messages = new MessageList(List.of(
           new TemplateMessageSpec(MessageType.SYSTEM,
                   staticTemplate("You are assistant to convert user questions to JSON. Wrap user question into valid JSON")),
           new TemplateMessageSpec(MessageType.USER, text("Why sky is blue?"))));

        val spec = ChatCallSpec.builder()
                .chatClientBuilder(defaultBuilder(chatBuilder))
                .messages(messages)
                .build();

        val map = spec.call().asMap();
        assertNotNull(map);

    }
}