package io.qpointz.mill.ai.nlsql.tools;

import io.qpointz.mill.ai.nlsql.ValueMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
// ToolCallback is part of Spring AI chat client API
// If import fails, ensure spring-ai-client-chat dependency is available

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ValueMappingTool.
 */
class ValueMappingToolTest {

    private ValueMapper mockValueMapper;
    private ValueMappingTool tool;

    @BeforeEach
    void setUp() {
        mockValueMapper = mock(ValueMapper.class);
        tool = new ValueMappingTool(mockValueMapper);
    }

    @Test
    void should_return_mapped_value_for_string_constant() {
        // Arrange
        var request = new ValueMappingTool.ValueMappingRequest(
            "MONETA.CLIENTS.SEGMENT",
            "premium",
            "string",
            "constant"
        );

        var placeholderMapping = new ValueMapper.PlaceholderMapping(
            "moneta_clients_segment_premium",
            "MONETA.CLIENTS.SEGMENT",
            "premium",
            "premium",
            "string",
            null,
            "constant"
        );

        var mappedValue = new ValueMapper.MappedValue(
            placeholderMapping,
            "Premium"
        );

        when(mockValueMapper.mapValue(any(ValueMapper.PlaceholderMapping.class)))
            .thenReturn(mappedValue);

        // Act
        var response = tool.apply(request);

        // Assert
        assertNotNull(response);
        assertEquals("Premium", response.mappedValue());
        assertEquals("MONETA.CLIENTS.SEGMENT", response.target());
        assertEquals("premium", response.display());
        assertTrue(response.meaning().contains("premium"));

        // Verify ValueMapper was called with correct mapping
        var captor = org.mockito.ArgumentCaptor.forClass(ValueMapper.PlaceholderMapping.class);
        verify(mockValueMapper).mapValue(captor.capture());
        var captured = captor.getValue();
        assertEquals("MONETA.CLIENTS.SEGMENT", captured.target());
        assertEquals("premium", captured.display());
        assertEquals("string", captured.type());
        assertEquals("constant", captured.kind());
    }

    @Test
    void should_return_mapped_value_for_pattern() {
        // Arrange
        var request = new ValueMappingTool.ValueMappingRequest(
            "CATALOG.PRODUCTS.NAME",
            "starts with Acme",
            "string",
            "pattern"
        );

        var placeholderMapping = new ValueMapper.PlaceholderMapping(
            "catalog_products_name_starts_with_acme",
            "CATALOG.PRODUCTS.NAME",
            "starts with Acme",
            "starts with Acme",
            "string",
            null,
            "pattern"
        );

        var mappedValue = new ValueMapper.MappedValue(
            placeholderMapping,
            "Acme%"
        );

        when(mockValueMapper.mapValue(any(ValueMapper.PlaceholderMapping.class)))
            .thenReturn(mappedValue);

        // Act
        var response = tool.apply(request);

        // Assert
        assertNotNull(response);
        assertEquals("Acme%", response.mappedValue());
        assertEquals("CATALOG.PRODUCTS.NAME", response.target());
        assertEquals("starts with Acme", response.display());
        assertEquals("pattern", request.kind());
    }

    @Test
    void should_return_mapped_value_for_number() {
        // Arrange
        var request = new ValueMappingTool.ValueMappingRequest(
            "SALES.ORDERS.QUANTITY",
            "100",
            "number",
            "constant"
        );

        var mappedValue = new ValueMapper.MappedValue(
            new ValueMapper.PlaceholderMapping(
                "sales_orders_quantity_100",
                "SALES.ORDERS.QUANTITY",
                "100",
                "100",
                "number",
                null,
                "constant"
            ),
            "100"
        );

        when(mockValueMapper.mapValue(any(ValueMapper.PlaceholderMapping.class)))
            .thenReturn(mappedValue);

        // Act
        var response = tool.apply(request);

        // Assert
        assertEquals("100", response.mappedValue());
        assertEquals("number", request.type());
    }

    @Test
    void should_return_mapped_value_for_date() {
        // Arrange
        var request = new ValueMappingTool.ValueMappingRequest(
            "ORDERS.ORDER_DATE",
            "2024-01-01",
            "date",
            "constant"
        );

        var mappedValue = new ValueMapper.MappedValue(
            new ValueMapper.PlaceholderMapping(
                "orders_order_date_2024_01_01",
                "ORDERS.ORDER_DATE",
                "2024-01-01",
                "2024-01-01",
                "date",
                null,
                "constant"
            ),
            "2024-01-01"
        );

        when(mockValueMapper.mapValue(any(ValueMapper.PlaceholderMapping.class)))
            .thenReturn(mappedValue);

        // Act
        var response = tool.apply(request);

        // Assert
        assertEquals("2024-01-01", response.mappedValue());
        assertEquals("date", request.type());
    }

    @Test
    void should_return_mapped_value_for_boolean() {
        // Arrange
        var request = new ValueMappingTool.ValueMappingRequest(
            "USERS.IS_ACTIVE",
            "true",
            "boolean",
            "constant"
        );

        var mappedValue = new ValueMapper.MappedValue(
            new ValueMapper.PlaceholderMapping(
                "users_is_active_true",
                "USERS.IS_ACTIVE",
                "true",
                "true",
                "boolean",
                null,
                "constant"
            ),
            "true"
        );

        when(mockValueMapper.mapValue(any(ValueMapper.PlaceholderMapping.class)))
            .thenReturn(mappedValue);

        // Act
        var response = tool.apply(request);

        // Assert
        assertEquals("true", response.mappedValue());
        assertEquals("boolean", request.type());
    }

    @Test
    void should_generate_placeholder_name_from_target_and_display() {
        // Arrange
        var request = new ValueMappingTool.ValueMappingRequest(
            "SCHEMA.TABLE.COLUMN",
            "test value",
            "string",
            "constant"
        );

        when(mockValueMapper.mapValue(any(ValueMapper.PlaceholderMapping.class)))
            .thenReturn(new ValueMapper.MappedValue(
                new ValueMapper.PlaceholderMapping(
                    "schema_table_column_test_value",
                    "SCHEMA.TABLE.COLUMN",
                    "test value",
                    "test value",
                    "string",
                    null,
                    "constant"
                ),
                "TestValue"
            ));

        // Act
        tool.apply(request);

        // Assert - verify placeholder name format
        var captor = org.mockito.ArgumentCaptor.forClass(ValueMapper.PlaceholderMapping.class);
        verify(mockValueMapper).mapValue(captor.capture());
        var captured = captor.getValue();
        assertTrue(captured.placeholder().startsWith("schema_table_column_"));
        assertTrue(captured.placeholder().contains("test_value"));
    }

    @Test
    void should_handle_special_characters_in_display() {
        // Arrange
        var request = new ValueMappingTool.ValueMappingRequest(
            "PRODUCTS.NAME",
            "test-value (special)",
            "string",
            "constant"
        );

        when(mockValueMapper.mapValue(any(ValueMapper.PlaceholderMapping.class)))
            .thenReturn(new ValueMapper.MappedValue(
                new ValueMapper.PlaceholderMapping(
                    "products_name_test_value_special_",
                    "PRODUCTS.NAME",
                    "test-value (special)",
                    "test-value (special)",
                    "string",
                    null,
                    "constant"
                ),
                "TestValue"
            ));

        // Act
        tool.apply(request);

        // Assert - verify special characters are normalized in placeholder name
        var captor = org.mockito.ArgumentCaptor.forClass(ValueMapper.PlaceholderMapping.class);
        verify(mockValueMapper).mapValue(captor.capture());
        var captured = captor.getValue();
        // Should not contain parentheses or hyphens in placeholder name
        assertFalse(captured.placeholder().contains("("));
        assertFalse(captured.placeholder().contains(")"));
    }
    
    @Test
    void should_handle_null_kind() {
        // Arrange
        var request = new ValueMappingTool.ValueMappingRequest(
            "TABLE.COLUMN",
            "value",
            "string",
            null
        );

        when(mockValueMapper.mapValue(any(ValueMapper.PlaceholderMapping.class)))
            .thenReturn(new ValueMapper.MappedValue(
                new ValueMapper.PlaceholderMapping(
                    "table_column_value",
                    "TABLE.COLUMN",
                    "value",
                    "value",
                    "string",
                    null,
                    null
                ),
                "Value"
            ));

        // Act
        var response = tool.apply(request);

        // Assert
        assertNotNull(response);
        assertEquals("Value", response.mappedValue());

        var captor = org.mockito.ArgumentCaptor.forClass(ValueMapper.PlaceholderMapping.class);
        verify(mockValueMapper).mapValue(captor.capture());
        assertNull(captor.getValue().kind());
    }

    @Test
    void should_echo_back_target_and_display_in_response() {
        // Arrange
        var request = new ValueMappingTool.ValueMappingRequest(
            "SCHEMA.TABLE.COLUMN",
            "user phrase",
            "string",
            "constant"
        );

        when(mockValueMapper.mapValue(any(ValueMapper.PlaceholderMapping.class)))
            .thenReturn(new ValueMapper.MappedValue(
                new ValueMapper.PlaceholderMapping(
                    "schema_table_column_user_phrase",
                    "SCHEMA.TABLE.COLUMN",
                    "user phrase",
                    "user phrase",
                    "string",
                    null,
                    "constant"
                ),
                "MappedValue"
            ));

        // Act
        var response = tool.apply(request);

        // Assert
        assertEquals("SCHEMA.TABLE.COLUMN", response.target());
        assertEquals("user phrase", response.display());
        assertEquals("MappedValue", response.mappedValue());
    }

    @Test
    void should_include_meaning_in_response() {
        // Arrange
        var request = new ValueMappingTool.ValueMappingRequest(
            "TABLE.COLUMN",
            "test",
            "string",
            "constant"
        );

        when(mockValueMapper.mapValue(any(ValueMapper.PlaceholderMapping.class)))
            .thenReturn(new ValueMapper.MappedValue(
                new ValueMapper.PlaceholderMapping(
                    "table_column_test",
                    "TABLE.COLUMN",
                    "test",
                    "test",
                    "string",
                    null,
                    "constant"
                ),
                "Test"
            ));

        // Act
        var response = tool.apply(request);

        // Assert
        assertNotNull(response.meaning());
        assertTrue(response.meaning().contains("test"));
    }
}
