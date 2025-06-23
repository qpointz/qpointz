package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.chat.messages.HashUtils;
import io.qpointz.mill.ai.nlsql.CallSpecsChatClientBuilders;
import io.qpointz.mill.ai.nlsql.ChatApplication;
import io.qpointz.mill.ai.nlsql.MessageSpecs;
import io.qpointz.mill.ai.nlsql.model.MessageRole;
import io.qpointz.mill.ai.nlsql.model.UserChatMessage;
import io.qpointz.mill.ai.nlsql.model.pojo.Chat;
import io.qpointz.mill.ai.nlsql.repositories.UserChatMessageRepository;
import io.qpointz.mill.services.annotations.ConditionalOnService;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.services.metadata.MetadataProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@Slf4j
@ConditionalOnService("ai-nl2data")
public class ChatProcessor {

    private final UserChatMessageRepository userChatMessageRepository;
    private final ChatMemory chatMemory;
    private final MetadataProvider metadataProvider;
    private final DataOperationDispatcher dataOperationDispatcher;
    private final ChatClient.Builder chatBuilder;

    public ChatProcessor(MetadataProvider metadataProvider,
                         DataOperationDispatcher dataOperationDispatcher,
                         UserChatMessageRepository userChatMessageRepository,
                         ChatMemory chatMemory, 
                         ChatClient.Builder chatBuilder) {
        this.userChatMessageRepository = userChatMessageRepository;
        this.chatMemory = chatMemory;
        this.chatBuilder = chatBuilder;
        this.metadataProvider = metadataProvider;
        this.dataOperationDispatcher = dataOperationDispatcher;
    }

    //try {
           /* val wf = new ChatTaskWorkflow(chatModel, chatId, metadataProvider, dataOperationDispatcher, chatMemory);
            val res = wf.call(request.message());
            val um = this.userChatMessageRepository.save(UserChatMessage.builder()
                    .role(MessageRole.CHAT)
                    .message(res.reasoning().explanation())
                    .contentFrom(res.result())
                    .userChat(chat)
                    .build());
            return Optional.of(um.toPojo());*/




    public void processRequest(Chat.SendChatMessageRequest request, ChatSession chat) {
        chat.sendEvent(request);

        val promptHashes = this.chatMemory
                .get(chat.conversationId())
                .stream()
                .map(k-> HashUtils.digest(k.getText()))
                .collect(Collectors.toSet());


        val application = new ChatApplication(chat.getChatBuilders(),
                                metadataProvider,
                                dataOperationDispatcher,
                promptHashes);

        val resp = application
                .query(request.message())
                .asMap();

        val responseBuilder = UserChatMessage.builder()
                .role(MessageRole.CHAT)
                .contentFrom(resp)
                .userChat(chat.getChat());

        val explanation = resp.getOrDefault("explanation", "")
                .toString();

        responseBuilder.message(explanation);

        val um = this.userChatMessageRepository.save(responseBuilder.build());

        chat.sendEvent(um.toPojo());
    }

}
