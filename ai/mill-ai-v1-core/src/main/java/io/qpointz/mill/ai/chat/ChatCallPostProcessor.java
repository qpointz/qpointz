package io.qpointz.mill.ai.chat;

import java.util.Map;

public interface ChatCallPostProcessor {

    Map<String, Object> process(Map<String, Object> result);

}
