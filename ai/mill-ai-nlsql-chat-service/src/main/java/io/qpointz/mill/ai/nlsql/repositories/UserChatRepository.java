package io.qpointz.mill.ai.nlsql.repositories;

import io.qpointz.mill.ai.nlsql.model.UserChat;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

@Transactional
public interface UserChatRepository extends CrudRepository<UserChat, UUID> {

    /**
     * Lists chats for a user ordered by favorite then creation time.
     */
    @Query("SELECT u FROM UserChat u WHERE u.userName = ?1 ORDER BY u.isFavorite DESC, u.created DESC")
    List<UserChat> chatsByUser(String status);

    /**
     * Fetches chats by id (legacy accessor).
     */
    List<UserChat> getUserChatByIdIs(UUID id);
}
