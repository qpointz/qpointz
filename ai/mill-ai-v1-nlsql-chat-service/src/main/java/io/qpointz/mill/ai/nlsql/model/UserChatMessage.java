package io.qpointz.mill.ai.nlsql.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.ai.nlsql.model.pojo.ChatMessage;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import static io.qpointz.mill.utils.JsonUtils.defaultJsonMapper;

@Slf4j
@Entity
@Table(name = UserChatMessage.TABLE_NAME/*, indexes = {
        @Index(name = "ix_user_chat_id", columnList = "id", unique = true),
        @Index(name = "ix_user_chat_user_name", columnList = "userName"),
        @Index(name = "ix_user_chat_created", columnList = "created")
}*/)
@AllArgsConstructor
@NoArgsConstructor
@Builder
/**
 * Entity capturing chat messages with serialized content payloads.
 */
public class UserChatMessage {


    public static final String TABLE_NAME = "user_chat_message";

    /**
     * Converts this entity into its DTO representation.
     */
    public ChatMessage toPojo()  {
        try {
            return new ChatMessage(this.id, this.message, this.role, this.contentAsMap());
        } catch (JsonProcessingException e) {
            throw new MillRuntimeException(e);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Getter
    @Setter
    private UUID id;

    @Getter
    @Setter
    @Lob
    private String message;

    @Getter
    @Setter
    @Lob
    @Column
    private String content;

    @Getter
    @Setter
    @Builder.Default
    @JsonFormat(pattern = "uuuu-MM-dd'T'HH:mm:ssXXX" )
    private ZonedDateTime created = ZonedDateTime.now();

    @Getter
    @Setter
    private MessageRole role;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = UserChat.class)
    @JsonIgnore
    @Getter
    @Setter
    private UserChat userChat;

    /**
     * Parses the persisted JSON content into a map.
     */
    public Map<String, Object> contentAsMap() throws JsonProcessingException {
        return defaultJsonMapper().readValue(this.content, Map.class);
    }

    public static class UserChatMessageBuilder {

        /**
         * Serializes the given object into JSON and stores it as message content.
         */
        public <T> UserChatMessageBuilder  contentFrom(T entity) {
            try {
                String jsonString = defaultJsonMapper().writeValueAsString(entity);
                return this.content(jsonString);
            } catch (JsonProcessingException e) {
                throw MillRuntimeException.of(e);
            }
        }
    }



}
