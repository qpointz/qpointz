package io.qpointz.mill.ai.nlsql.tools;

import io.qpointz.mill.ai.nlsql.ValueMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.function.Function;

/**
 * Spring AI tool for value mapping that allows the LLM to resolve domain values
 * during SQL generation instead of emitting placeholders that require postprocessing.
 * 
 * This tool bridges the existing {@link ValueMapper} interface to Spring AI's
 * function-calling mechanism, enabling the LLM to call back for value resolution
 * while generating SQL queries.
 * 
 * Usage Example - Integration with CallSpecsChatClientBuilders:
 * <pre>{@code
 * public ChatClientBuilder conversationChat() {
 *     ToolCallback valueMappingTool = ValueMappingTool.createToolCallback(valueMapper);
 *     
 *     return ChatClientBuilders.defaultBuilder(
 *         ChatClient.builder(this.chatModel)
 *             .defaultToolCallbacks(valueMappingTool)
 *             .defaultAdvisors(
 *                 MessageChatMemoryAdvisor.builder(this.chatMemory)
 *                     .conversationId(this.conversationId)
 *                     .build()
 *             )
 *     );
 * }
 * }</pre>
 * 
 * When the LLM generates SQL, it will call this tool instead of emitting placeholders.
 * The tool returns the resolved value, which the LLM inserts directly into the SQL.
 */
@Slf4j
@AllArgsConstructor
public class ValueMappingTool implements Function<ValueMappingTool.ValueMappingRequest, ValueMappingTool.ValueMappingResponse> {

    private final ValueMapper valueMapper;

    /**
     * Request structure for the value mapping tool.
     */
    public record ValueMappingRequest(
        String target,           // e.g., "MONETA.CLIENTS.SEGMENT"
        String display,          // e.g., "premium" (user's phrase)
        String type,             // e.g., "string", "number", "date", "boolean"
        String kind              // e.g., "constant", "pattern"
    ) {}

    /**
     * Response structure returned by the tool.
     */
    public record ValueMappingResponse(
        String mappedValue,      // The actual value to use in SQL (e.g., "Premium")
        String target,           // Echo back the target column
        String display,          // Echo back the user's phrase
        String meaning           // Optional explanation of the mapping
    ) {}

//    @Tool(name="value-mapping-tool", description =  "Resolve a domain value to its actual database value. " +
//            "Use this tool whenever you need to convert a user-provided value into the exact value stored in the database. " +
//            "This applies to all constant values: strings, numbers, dates, booleans, LIKE/ILIKE patterns, regex patterns, and range boundaries (BETWEEN, >=, <=, etc.). " +
//            "The tool takes: target (fully qualified column name in SCHEMA.TABLE.COLUMN format), " +
//            "display (user-facing term or phrase from the original input), " +
//            "type (string | number | date | boolean), " +
//            "and kind (constant for exact matches like =, IN, BETWEEN, or pattern for LIKE/ILIKE/regex conditions). " +
//            "Returns the mapped value as it will appear in SQL without any quotes or escaping. " +
//            "The resolved value must equal the literal value as it appears logically in SQL, but without surrounding quotes or escaping.")
    public ValueMappingResponse apply(@ToolParam(description = "mapping request parameters providing target (attribute in table value mapped to") ValueMappingRequest request) {
        log.info("Tool called: (fc) resolveValue(target={}, display={}, type={}, kind={})",
            request.target(), request.display(), request.type(), request.kind());

        // Convert request to PlaceholderMapping format expected by ValueMapper
        // Note: placeholder name is not used in tool mode, but required by the structure
        String placeholderName = request.target().replace(".", "_").toLowerCase() + "_" + 
                                 request.display().toLowerCase().replaceAll("[^a-z0-9]", "_");
        
        ValueMapper.PlaceholderMapping mapping = new ValueMapper.PlaceholderMapping(
            placeholderName,           // Generate a placeholder name (not used in tool mode)
            request.target(),
            request.display(),
            request.display(),          // Use display as initial resolved value
            request.type(),
            null,                       // meaning - optional, can be null
            request.kind()              // kind - optional
        );

        // Delegate to existing ValueMapper
        ValueMapper.MappedValue mapped = valueMapper.mapValue(mapping);

        return new ValueMappingResponse(
            mapped.mappedValue(),
            request.target(),
            request.display(),
            "Mapped value for " + request.display()
        );
    }

    public static ToolCallback createToolCallback(ValueMapper valueMapper) {
        val description = "Resolve a domain value to its actual database value. " +
            "Use this tool whenever you need to convert a user-provided value into the exact value stored in the database. " +
            "This applies to all constant values: strings, numbers, dates, booleans, LIKE/ILIKE patterns, regex patterns, and range boundaries (BETWEEN, >=, <=, etc.). " +
            "The tool takes: target (fully qualified column name in SCHEMA.TABLE.COLUMN format), " +
            "display (user-facing term or phrase from the original input), " +
            "type (string | number | date | boolean), " +
            "and kind (constant for exact matches like =, IN, BETWEEN, or pattern for LIKE/ILIKE/regex conditions). " +
            "Returns the mapped value as it will appear in SQL without any quotes or escaping. " +
            "The resolved value must equal the literal value as it appears logically in SQL, but without surrounding quotes or escaping.";
        log.info("Create function callback");
        return FunctionToolCallback.builder("value-mapping-tool", new ValueMappingTool(valueMapper) )
                .description(description)
                .inputType(ValueMappingTool.ValueMappingRequest.class)
                .build();

    }

    /**
     * Factory method to create a ToolCallback for Spring AI ChatClient.
     * 
     * NOTE: This method uses reflection to work around compilation issues when Spring AI tool classes
     * are not available in the classpath. Once the proper Spring AI version with tool support is available,
     * this should be refactored to use direct imports.
     * 
     * @param valueMapper the value mapper to use for resolving values
     * @return a ToolCallback that can be registered with ChatClient, or null if tool classes are not available
     */
//    @SuppressWarnings({"unchecked", "rawtypes"})
//    public static  createToolCallback(ValueMapper valueMapper) {
//        try {
//            ValueMappingTool tool = new ValueMappingTool(valueMapper);
//
//            // Use reflection to access Spring AI tool classes
//            // This allows compilation even if the classes are not in the classpath
//            Class<?> functionToolCallbackClass = Class.forName("org.springframework.ai.chat.client.FunctionToolCallback");
//            Object builder = functionToolCallbackClass.getMethod("builder", String.class, Function.class)
//                    .invoke(null, "resolveValue", tool);
//
//            String description = "Resolve a domain value to its actual database value. " +
//                    "Use this tool whenever you need to convert a user-provided value into the exact value stored in the database. " +
//                    "This applies to all constant values: strings, numbers, dates, booleans, LIKE/ILIKE patterns, regex patterns, and range boundaries (BETWEEN, >=, <=, etc.). " +
//                    "The tool takes: target (fully qualified column name in SCHEMA.TABLE.COLUMN format), " +
//                    "display (user-facing term or phrase from the original input), " +
//                    "type (string | number | date | boolean), " +
//                    "and kind (constant for exact matches like =, IN, BETWEEN, or pattern for LIKE/ILIKE/regex conditions). " +
//                    "Returns the mapped value as it will appear in SQL without any quotes or escaping. " +
//                    "The resolved value must equal the literal value as it appears logically in SQL, but without surrounding quotes or escaping.";
//
//            builder = builder.getClass().getMethod("description", String.class).invoke(builder, description);
//            builder = builder.getClass().getMethod("inputType", Class.class).invoke(builder, ValueMappingRequest.class);
//            return builder.getClass().getMethod("build").invoke(builder);
//        } catch (ClassNotFoundException | NoSuchMethodException e) {
//            // Spring AI tool classes are not available in this version
//            log.warn("Spring AI tool classes not available. Value mapping tool will not be registered. " +
//                    "Ensure Spring AI version with tool support is included in dependencies.");
//            return null;
//        } catch (Exception e) {
//            log.error("Failed to create value mapping tool callback", e);
//            return null;
//        }
//    }
}
