//package io.qpointz.mill.ai.nlsql.components.tasks;
//
//import io.qpointz.mill.ai.chat.prompts.templates.CompositePromptTemplate;
//import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
//import io.qpointz.mill.ai.nlsql.SchemaScope;
//import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
//import io.qpointz.mill.services.metadata.MetadataProvider;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.extern.slf4j.Slf4j;
//import lombok.val;
//import org.junit.jupiter.api.Test;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.UUID;
//
//import static io.qpointz.mill.ai.chat.prompts.PromptTemplates.staticTemplate;
//import static io.qpointz.mill.ai.chat.prompts.templates.CompositePromptTemplate.compositeItem;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@Slf4j
//@SpringBootTest(classes = {ExecQueryTaskTest.class, DefaultServiceConfiguration.class})
//@ComponentScan("io.qpointz")
//@ActiveProfiles("test-moneta-slim")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//public class ExecQueryTaskTest {
//
//    @Autowired
//    MetadataProvider metadataProvider;
//
//    @Builder
//    private static class TaskImpl extends AbstractSchemaTask {
//
//        @Getter
//        private final List<ReasoningResponse.IntentTable> requiredTables;
//
//        @Getter
//        private final SchemaScope schemaScope;
//
//        @Getter
//        protected final String user;
//
//        @Getter
//        protected final MetadataProvider metadataProvider;
//
//        @Getter
//        protected final ChatClient chatClient;
//
//        @Getter
//        private final UUID chatId;
//
//        @Override
//        public CompositePromptTemplate.Item getUserPromptTemplateItem() throws IOException {
//            return compositeItem(staticTemplate("\n!!main user!!\n"));
//        }
//
//    }
//
//    @Test
//    void trivial() throws IOException {
//        val task = TaskImpl.builder()
//                .user("how many clients")
//                .requiredTables(List.of())
//                .metadataProvider(metadataProvider)
//                .schemaScope(SchemaScope.FULL)
//                .build();
//        val prompt = task.getUserPromptTemplate();
//        log.info(prompt.render());
//    }
//
//    @Test
//    void containsMainUserPrompt() throws IOException {
//        val task = TaskImpl.builder()
//                .user("how many clients")
//                .requiredTables(List.of())
//                .schemaScope(SchemaScope.FULL)
//                .metadataProvider(metadataProvider)
//                .build();
//        val prompt = task.getUserPromptTemplate().render();
//        assertTrue(prompt.contains("!!main user!!"));
//        assertTrue(prompt.contains("MONETA.LOAN"));
//        assertTrue(prompt.contains("MONETA.LOANS -> MONETA.LOAN_PAYMENTS"));
//        log.info(prompt);
//    }
//
//    @Test
//    void noneSchemaTest() throws IOException {
//        val task = TaskImpl.builder()
//                .user("how many clients")
//                .requiredTables(List.of())
//                .schemaScope(SchemaScope.NONE)
//                .metadataProvider(metadataProvider)
//                .build();
//        val prompt = task.getUserPromptTemplate().render();
//        assertTrue(prompt.contains("!!main user!!"));
//        assertFalse(prompt.contains("MONETA.LOAN"));
//        assertFalse(prompt.contains("MONETA.LOANS -> MONETA.LOAN_PAYMENTS"));
//        log.info(prompt);
//    }
//
//    @Test
//    void partialSchemaTest() throws IOException {
//        val intentTables = List.of(
//                new ReasoningResponse.IntentTable("MONETA", "CLIENTS", false, false),
//                new ReasoningResponse.IntentTable("MONETA", "ACCOUNTS", false, false)
//        );
//
//        val task = TaskImpl.builder()
//                .user("how many clients")
//                .requiredTables(intentTables)
//                .schemaScope(SchemaScope.PARTIAL)
//                .metadataProvider(metadataProvider)
//                .build();
//
//        val prompt = task.getUserPromptTemplate().render();
//        assertTrue(prompt.contains("!!main user!!"));
//        assertTrue(prompt.contains("MONETA.CLIENTS"));
//        assertTrue(prompt.contains("CLIENT_ID"));
//        assertTrue(prompt.contains("MONETA.CLIENTS -> MONETA.ACCOUNTS"));
//
//        assertFalse(prompt.contains("MONETA.LOAN"));
//        assertFalse(prompt.contains("MONETA.LOANS -> MONETA.LOAN_PAYMENTS"));
//        log.info(prompt);
//    }
//}