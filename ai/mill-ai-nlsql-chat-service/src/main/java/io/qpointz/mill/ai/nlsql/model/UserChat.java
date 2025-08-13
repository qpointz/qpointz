package io.qpointz.mill.ai.nlsql.model;

import aj.org.objectweb.asm.TypeReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qpointz.mill.ai.nlsql.model.pojo.Chat;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

@Entity
@Table(name = UserChat.TABLE_NAME, indexes = {
        @Index(name = "ix_user_chat_id", columnList = "id", unique = true),
        @Index(name = "ix_user_chat_user_name", columnList = "userName"),
        @Index(name = "ix_user_chat_created", columnList = "created")
})
@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserChat {

    public static final String TABLE_NAME = "user_chat";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Getter
    @Setter
    private UUID id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String userName;

    @Getter
    @Setter
    @Builder.Default
    @JsonFormat(pattern = "uuuu-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime created = ZonedDateTime.now();

    @Getter
    @Setter
    @Builder.Default
    private Boolean isFavorite = false;

    @OneToMany(
            mappedBy = "userChat",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Getter
    @Setter
    @Builder.Default
    private List<UserChatMessage> chatMessages = new ArrayList<>();

    public Chat toPojo() {
        return new Chat(this.id, this.name, this.isFavorite, this.created);
    }
}
