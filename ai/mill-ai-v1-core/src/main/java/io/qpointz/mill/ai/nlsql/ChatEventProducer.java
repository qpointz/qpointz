package io.qpointz.mill.ai.nlsql;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

public abstract class ChatEventProducer {

    public abstract <T> void sendEvent(T entity, String event);

    private record ProgressEvent<T>(T entity, String message, Instant timestamp, String status) {
    }

    private record ErrorEvent<T>(T entity, String message, Instant timestamp) {
    }

    public <T> void beginProgressEvent(T entity) {
        this.sendEvent(new ProgressEvent(entity, entity.toString(), Instant.now(), "begin"), "chat_begin_progress_event");
    }

    public <T> void endProgressEvent() {
        this.sendEvent(new ProgressEvent(null, null, Instant.now(), "end"), "chat_end_progress_event");
    }


    public <T> void chatErrorEvent(T entity) {
        this.sendEvent(new ErrorEvent(entity, entity.toString(), Instant.now()), "chat_error_event");
    }

    @Slf4j
    protected static final class DefaultChatEventProducer extends ChatEventProducer {

        @Override
        public <T> void sendEvent(T entity, String event) {
            log.info("[Chat Event:{}] {}", event, entity.toString());
        }
    }

    public static final ChatEventProducer DEFAULT = new DefaultChatEventProducer();

}
