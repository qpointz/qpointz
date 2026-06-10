package io.qpointz.mill.ai.nlsql.processors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.qpointz.mill.ai.chat.ChatCallPostProcessor;
import io.qpointz.mill.ai.nlsql.IntentSpecs;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

import static io.qpointz.mill.ai.nlsql.ChatCallBase.applyPostProcessors;

@Slf4j
@AllArgsConstructor
public class RefineIntentProcessor implements ChatCallPostProcessor {

    private final ReasoningResponse response;

    private final IntentSpecs specs;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RefineResult(
            @JsonProperty(value = "original-intent", required = false) String originalIntent,
            @JsonProperty(value = "refine-intent", required = false) String refineIntent) {
    }

    @Override
    public Map<String, Object> process(Map<String, Object> rawResult) {
        var mayBeRefine = rawResult.getOrDefault("refine", null);
        if (mayBeRefine == null) {
            log.warn("Refine intent processor called without 'mayBeRefine' key in the rawResult. Returning original rawResult.");
            return rawResult;
        }

        val refine = JsonUtils.defaultJsonMapper()
                .convertValue(mayBeRefine, RefineResult.class);

        log.debug("Refine intent processor called with: {}", refine);

        var intentKey = refine.refineIntent != null
                ? refine.refineIntent
                : refine.originalIntent;

        if (intentKey.equalsIgnoreCase(IntentSpecs.REFINE_QUERY_INTENT_KEY)) {
            log.debug("Refine intent is 'refine-query', returning original rawResult.");
            return rawResult;
        }

        val modified = new HashMap<>(rawResult);
        modified.put("resultIntent", intentKey);

        try {
            val intent = this.specs.getIntent(intentKey);
            return applyPostProcessors(modified, intent.getPostProcessors(this.response));
        } catch (Exception ex) {
            modified.put("error", ex.getMessage());
            return modified; // Return the original rawResult if postprocessing fails
        }

    }
}
