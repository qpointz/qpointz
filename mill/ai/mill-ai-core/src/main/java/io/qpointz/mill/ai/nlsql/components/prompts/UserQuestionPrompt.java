//package io.qpointz.mill.ai.nlsql.components.prompts;
//
//import io.pebbletemplates.pebble.template.PebbleTemplate;
//import io.qpointz.mill.ai.chat.prompts.templates.PebblePromptTemplate;
//import lombok.AllArgsConstructor;
//
//import java.io.IOException;
//import java.util.Map;
//
//@AllArgsConstructor
//public class UserQuestionPrompt extends PebblePromptTemplate {
//
//    private final String userQuery;
//
//    @Override
//    protected PebbleTemplate getTemplate() {
//        try {
//            return PebblePromptTemplate.createInlineTemplate("prompts/user-question.prompt",
//                    UserQuestionPrompt.class.getClassLoader());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    protected Map<String, Object> applyValues() {
//        return Map.of("userQuestion", this.userQuery);
//    }
//}
