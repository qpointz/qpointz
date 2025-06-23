package io.qpointz.mill.ai.nlsql.processors;

import io.qpointz.mill.ai.chat.ChatCallPostProcessor;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

public class CheckIntentPresentProcessor implements ChatCallPostProcessor {
    @Override
    public Map<String, Object> process(Map<String, Object> result) {
        val hashMap = new HashMap<String, Object>();
        if (result != null) {
            hashMap.putAll(result);
        }

        if (hashMap.containsKey("intent")) {
            return hashMap;
        }

        hashMap.put("intent", "unsupported");

        return hashMap;
    }
}
