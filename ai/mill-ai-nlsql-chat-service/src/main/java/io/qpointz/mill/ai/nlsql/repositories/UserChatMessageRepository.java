package io.qpointz.mill.ai.nlsql.repositories;

import io.qpointz.mill.ai.nlsql.model.UserChatMessage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

@Transactional
public interface UserChatMessageRepository extends CrudRepository<UserChatMessage, Long> {

    @Query("SELECT u FROM UserChatMessage u WHERE u.userChat.id = ?1 ORDER BY u.created ASC")
    List<UserChatMessage> listMessagesByChatId(UUID chatId);
}
