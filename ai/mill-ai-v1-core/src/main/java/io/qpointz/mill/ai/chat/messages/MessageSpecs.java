package io.qpointz.mill.ai.chat.messages;

import io.qpointz.mill.ai.chat.messages.specs.TemplateMessageSpec;
import io.qpointz.mill.ai.nlsql.messages.specs.SchemaMessageSpec;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.data.backend.metadata.MetadataProvider;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;

import static io.qpointz.mill.ai.chat.messages.MessageTemplates.staticTemplate;

public class MessageSpecs {

    public MessageSpec reasonSystem() {
        return new TemplateMessageSpec(MessageType.SYSTEM,
                                        staticTemplate("templates/nlsql/reason/system.prompt",
                                        MessageSpecs.class));
    }

    public MessageSpec schemaShort(MetadataProvider metadataProvider) {
        return SchemaMessageSpec.builder(MessageType.USER,
                                        metadataProvider)
                .includeRelations(true)
                .includeRelationExpressions(false)
                .includeAttributes(false)
                .build();
    }

    public MessageSpec schema(MetadataProvider metadataProvider) {
        return schema(metadataProvider, List.of());
    }

    public MessageSpec schema(MetadataProvider metadataProvider, List<ReasoningResponse.IntentTable> requieredTables) {
        return SchemaMessageSpec.builder(MessageType.USER,
                        metadataProvider)
                .includeRelations(true)
                .includeRelationExpressions(true)
                .includeAttributes(true)
                .requiredTables(requieredTables)
                .build();
    }


}
