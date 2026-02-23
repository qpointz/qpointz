package io.qpointz.mill.ai.chat.messages;

import io.qpointz.mill.ai.chat.messages.specs.TemplateMessageSpec;
import io.qpointz.mill.ai.nlsql.messages.specs.SchemaMessageSpec;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.metadata.service.MetadataService;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;

import static io.qpointz.mill.ai.chat.messages.MessageTemplates.staticTemplate;

public class MessageSpecs {

    public MessageSpec reasonSystem() {
        return new TemplateMessageSpec(MessageType.SYSTEM,
                                        staticTemplate("templates/nlsql/reason/system.prompt",
                                        MessageSpecs.class));
    }

    public MessageSpec schemaShort(MetadataService metadataService) {
        return SchemaMessageSpec.builder(MessageType.USER,
                                        metadataService)
                .includeRelations(true)
                .includeRelationExpressions(false)
                .includeAttributes(false)
                .build();
    }

    public MessageSpec schema(MetadataService metadataService) {
        return schema(metadataService, List.of());
    }

    public MessageSpec schema(MetadataService metadataService, List<ReasoningResponse.IntentTable> requieredTables) {
        return SchemaMessageSpec.builder(MessageType.USER,
                        metadataService)
                .includeRelations(true)
                .includeRelationExpressions(true)
                .includeAttributes(true)
                .requiredTables(requieredTables)
                .build();
    }
}
