package io.qpointz.mill.ai.chat.messages;

import io.qpointz.mill.ai.chat.messages.specs.TemplateMessageSpec;
import io.qpointz.mill.ai.nlsql.messages.specs.SchemaMessageSpec;
import io.qpointz.mill.ai.nlsql.metadata.SchemaMessageMetadataPorts;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;

import static io.qpointz.mill.ai.chat.messages.MessageTemplates.staticTemplate;

public class MessageSpecs {

    public MessageSpec reasonSystem() {
        return new TemplateMessageSpec(MessageType.SYSTEM,
                                        staticTemplate("templates/nlsql/reason/system.prompt",
                                        MessageSpecs.class));
    }

    public MessageSpec schemaShort(SchemaMessageMetadataPorts schemaPorts) {
        return SchemaMessageSpec.forMetadata(MessageType.USER, schemaPorts)
                .includeRelations(true)
                .includeRelationExpressions(false)
                .includeAttributes(false)
                .build();
    }

    public MessageSpec schema(SchemaMessageMetadataPorts schemaPorts) {
        return schema(schemaPorts, List.of());
    }

    public MessageSpec schema(SchemaMessageMetadataPorts schemaPorts, List<ReasoningResponse.IntentTable> requieredTables) {
        return SchemaMessageSpec.forMetadata(MessageType.USER, schemaPorts)
                .includeRelations(true)
                .includeRelationExpressions(true)
                .includeAttributes(true)
                .requiredTables(requieredTables)
                .build();
    }
}
