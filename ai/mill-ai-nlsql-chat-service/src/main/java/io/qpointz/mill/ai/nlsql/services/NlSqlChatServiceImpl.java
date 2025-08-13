package io.qpointz.mill.ai.nlsql.services;

//import io.qpointz.mill.ai.nlsql.components.ChatTaskWorkflow;
import io.qpointz.mill.ai.nlsql.components.ChatProcessor;
import io.qpointz.mill.ai.nlsql.components.ChatSession;
import io.qpointz.mill.ai.nlsql.components.ChatSessionManager;
import io.qpointz.mill.ai.nlsql.model.MessageRole;
import io.qpointz.mill.ai.nlsql.model.UserChat;
import io.qpointz.mill.ai.nlsql.model.UserChatMessage;
import io.qpointz.mill.ai.nlsql.model.pojo.Chat;
import io.qpointz.mill.ai.nlsql.model.pojo.ChatMessage;
import io.qpointz.mill.ai.nlsql.repositories.UserChatMessageRepository;
import io.qpointz.mill.ai.nlsql.repositories.UserChatRepository;
import io.qpointz.mill.services.annotations.ConditionalOnService;
import lombok.val;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.codec.ServerSentEvent;
        import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

        import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@ConditionalOnService("ai-nl2data")
public class NlSqlChatServiceImpl implements NlSqlChatService {

    private static final String ANON_USER = "<ANONYMOUS>";

    private final UserChatRepository userChatRepository;
    private final UserChatMessageRepository userChatMessageRepository;


    private final ChatSessionManager chatSessionManager;
    private final ChatProcessor chatProcessor;

    public NlSqlChatServiceImpl(UserChatRepository userChatRepository,
                                UserChatMessageRepository userChatMessageRepository,
                                ChatProcessor chatProcessor,
                                ChatModel chatModel,
                                ChatMemory chatMemory) {
        this.userChatRepository = userChatRepository;
        this.userChatMessageRepository = userChatMessageRepository;
        this.chatSessionManager = new ChatSessionManager(chatModel, chatMemory);
        this.chatProcessor = chatProcessor;
    }

    private static String getUserName() {
        val ctx = SecurityContextHolder.getContext();
        val auth = ctx.getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ANON_USER;
        }
        return auth.getName();
    }

    @Override
    public List<Chat> listChats() {
        return this.userChatRepository
                .chatsByUser(getUserName()).stream()
                .map(UserChat::toPojo)
                .toList();
    }

    @Override
    public Optional<Chat> getChat(UUID chatId) {
        return this.userChatRepository
                .findById(chatId)
                .map(UserChat::toPojo);
    }

    @Override
    public Optional<Chat> updateChat(UUID chatId, Chat.UpdateChatRequest request) {
        val uc = this.userChatRepository.findById(chatId);
        return uc.map(k-> {
            if (request.chatName().isPresent()) {
                k.setName(request.chatName().get());
            }

            if (request.isFavorite().isPresent()) {
                k.setIsFavorite(request.isFavorite().get());
            }

            return this.userChatRepository.save(k);
        })
        .map(UserChat::toPojo);
    }

    @Override
    public boolean deleteChat(UUID chatId) {
        val uc = this.userChatRepository
                .findById(chatId);
        if (uc.isPresent()) {
            this.userChatRepository.delete(uc.get());
            return true;
        }
        return false;
    }

    @Override
    public Optional<List<ChatMessage>> listChatMessages(UUID chatId) {
        val mayBeChat = this.userChatRepository.findById(chatId);

        if (mayBeChat.isEmpty()) {
            return Optional.empty();
        }

        val messages = this.userChatMessageRepository.listMessagesByChatId(chatId)
                .stream()
                .map(UserChatMessage::toPojo)
                .toList();

        return Optional.of(messages);
    }


    @Override
    public Chat createChat(Chat.CreateChatRequest request) {
        val userChat =  this.userChatRepository.save(UserChat.builder()
                .name(request.name())
                .userName(getUserName())
                .build());
        val session = this.chatSessionManager.getOrCreate(userChat);

        val postRequest = new Chat.SendChatMessageRequest(request.name(), Map.of());

        val userChatMessage = this.userChatMessageRepository
                .save(UserChatMessage.builder()
                        .role(MessageRole.USER)
                        .message(postRequest.message())
                        .contentFrom(postRequest.content())
                        .userChat(session.getChat())
                        .build());

        session.sendEvent(userChatMessage);

        chatProcessor.processRequest(postRequest, session);

        return userChat.toPojo();
    }


    private Optional<ChatSession> getSession(UUID chatId) {
        val mayBeChat =  this.userChatRepository
                .findById(chatId);

        if (mayBeChat.isEmpty()) {
            return Optional.empty();
        }

        val chat = mayBeChat.get();

        return Optional.of(this.chatSessionManager
                .getOrCreate(chat));
    }


    @Override
    public Optional<ChatMessage> postChatMessage(UUID chatId, Chat.SendChatMessageRequest request) {
        return getSession(chatId).map(session -> {
                    val userChatMessage = this.userChatMessageRepository.save(UserChatMessage.builder()
                            .role(MessageRole.USER)
                            .message(request.message())
                            .contentFrom(request.content())
                            .userChat(session.getChat())
                            .build());

                    session.sendEvent(userChatMessage);

                    chatProcessor.processRequest(request, session);

                    return userChatMessage.toPojo();
                });
    }

    @Override
    public Optional<Flux<ServerSentEvent<?>>> chatStrtream(UUID chatId) {
        return getSession(chatId).map(ChatSession::stream);
    }
}
