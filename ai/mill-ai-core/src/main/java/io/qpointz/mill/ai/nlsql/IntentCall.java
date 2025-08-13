package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatClientBuilder;
import io.qpointz.mill.ai.chat.messages.MessageList;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class IntentCall extends ChatCallBase {

    @Getter
    private ReasoningResponse reason;

    @Getter
    private IntentSpecs intentSpecs;

    @Getter
    private ChatClientBuilder chatClientBuilder;

    @Getter
    private MessageList messages;

    @Getter
    private MessageSelector messageSelector;

    @Override
    protected Map<String, Object> postProcess(Map<String, Object> rawResult) {
        val rowIntent = rawResult.getOrDefault("intent", null);

        if (rowIntent != null) {
            log.debug("Intent found in the response, applying postprocessing as {}.", rowIntent);
            return postProcessByIntent(rawResult, rowIntent.toString());
        }

        log.debug("Applying postprocessing by original intent '{}'.", reason.intent());

        val intentKey = probeIntent(rawResult, reason);

        return postProcessByIntent(rawResult, intentKey);

    }

    private String probeIntent(Map<String, Object> rawResult, ReasoningResponse reason) {
        return rawResult
                .getOrDefault("refineAs", reason.intent())
                .toString();
    }

    private Map<String, Object> postProcessByIntent(Map<String, Object> rawResult, String intentKey) {
        try {
            val intent = this.intentSpecs.getIntent(intentKey);
            return applyPostProcessors(rawResult, intent.getPostProcessors(reason));
        } catch (Exception ex) {
            log.error("Failed to apply postprocessing for intent '{}'.", intentKey, ex);
            val map = new HashMap<>(rawResult);
            map.put("error", ex.getMessage());
            return rawResult; // Return the original result if postprocessing fails
        }
    }


}
