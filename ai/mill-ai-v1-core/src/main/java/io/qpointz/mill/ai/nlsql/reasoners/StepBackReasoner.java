package io.qpointz.mill.ai.nlsql.reasoners;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.qpointz.mill.ai.chat.ChatCall;
import io.qpointz.mill.ai.chat.ChatUserRequest;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import io.qpointz.mill.ai.nlsql.*;
import io.qpointz.mill.ai.nlsql.models.stepback.StepBackResponse;
import io.qpointz.mill.ai.nlsql.stepback.StepBackCall;
import io.qpointz.mill.metadata.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class StepBackReasoner implements Reasoner {

    private final CallSpecsChatClientBuilders chatBuilders;
    private final MetadataService metadataService;
    private final MessageSelector messageSelector;

    @Override
    public ReasoningReply reason(ChatUserRequest request) {
        if (request.reasoningId().isEmpty()) {
            return initialReasoning(request);
        }

        return continueReasoning(request);
    }

    private static Cache<String, StepBackResponse> reasoningCache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.of(5, ChronoUnit.MINUTES))
            .build();

    private ReasoningReply initialReasoning(ChatUserRequest request) {
        val query = request.query();
        val stepBackCall = new StepBackCall(query, this.chatBuilders.reasoningChat(),
                MessageSpecs.stepBack(query, metadataService), this.messageSelector);

        if (!needClarification(stepBackCall)) {
            val reasonCall = new ReasonCall(query,
                    this.chatBuilders.reasoningChat(),
                    MessageSpecs.reason(query, metadataService),
                    this.messageSelector);
            return ReasoningReply
                    .reasoned(ChatReply.reply(reasonCall));
        }

        return beginClarification(stepBackCall);
    }

    private boolean needClarification(ChatCall chatCall) {
        val raw = chatCall.asMap();
        return Optional.ofNullable(raw.get("need-clarification"))
                .map(Object::toString)
                .map(Boolean::parseBoolean)
                .orElse(false);
    }

    private ReasoningReply beginClarification(StepBackCall stepBackCall) {
        val raw = stepBackCall
                .as(StepBackResponse.class);
        reasoningCache.put(raw.reasoningId(), raw);
        return new ReasoningReply(ReasoningReply.ReasoningResult.CLARIFY,
                ChatReply.reply(stepBackCall),
                raw.reasoningId());
    }

    private ReasoningReply continueReasoning(ChatUserRequest request) {
        val lastResponse = request
                .reasoningId()
                .map(k -> reasoningCache.get(k, z -> StepBackResponse.empty()))
                .orElse(StepBackResponse.empty());

        String baseQuery = resolveBaseQuery(lastResponse, request);
        val stepBackCall = new StepBackCall(baseQuery, this.chatBuilders.reasoningChat(),
                MessageSpecs.stepBackWithClarification(baseQuery, request.query(), lastResponse, metadataService),
                this.messageSelector);

        if (!needClarification(stepBackCall)) {
            val reasonCall = new ReasonCall(baseQuery,
                    this.chatBuilders.reasoningChat(),
                    MessageSpecs.reason(baseQuery, metadataService),
                    this.messageSelector);
            return ReasoningReply
                    .reasoned(ChatReply.reply(reasonCall));
        }

        return beginClarification(stepBackCall);
    }

    private String resolveBaseQuery(StepBackResponse lastResponse, ChatUserRequest request) {
        if (lastResponse != null && lastResponse.query() != null && !lastResponse.query().isBlank()) {
            return lastResponse.query();
        }
        return request.query();
    }
}
