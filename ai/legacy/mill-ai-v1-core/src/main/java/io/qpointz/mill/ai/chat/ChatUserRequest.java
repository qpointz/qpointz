package io.qpointz.mill.ai.chat;

import java.util.Optional;

public interface ChatUserRequest {

    String query();

    Optional<String> reasoningId();

}
