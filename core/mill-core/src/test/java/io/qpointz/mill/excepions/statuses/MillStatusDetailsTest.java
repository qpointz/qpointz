package io.qpointz.mill.excepions.statuses;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MillStatusDetailsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldCaptureStatus_whenBuiltFromCheckedException() {
        var ex = MillStatuses.notFound("chat not found");
        var details = MillStatusDetails.of(ex);
        assertEquals(MillStatus.NOT_FOUND, details.getStatus());
    }

    @Test
    void shouldCaptureMessage_whenBuiltFromCheckedException() {
        var ex = MillStatuses.notFound("chat not found");
        var details = MillStatusDetails.of(ex);
        assertEquals("chat not found", details.getMessage());
    }

    @Test
    void shouldHaveNullDetails_whenBuiltWithoutDetails() {
        var ex = MillStatuses.notFound("chat not found");
        var details = MillStatusDetails.of(ex);
        assertNull(details.getDetails());
    }

    @Test
    void shouldCaptureDetails_whenDetailsProvided() {
        var ex = MillStatuses.badRequest("invalid input");
        var details = MillStatusDetails.of(ex, Map.of("field", "name"));
        assertEquals("name", details.getDetails().get("field"));
    }

    @Test
    void shouldCaptureStatus_whenBuiltFromRuntimeException() {
        var ex = MillStatuses.internalErrorRuntime("unexpected");
        var details = MillStatusDetails.of(ex);
        assertEquals(MillStatus.INTERNAL_ERROR, details.getStatus());
    }

    @Test
    void shouldSetTimestamp_whenCreated() {
        long before = System.currentTimeMillis();
        var details = MillStatusDetails.of(MillStatuses.notFound());
        long after = System.currentTimeMillis();
        assertTrue(details.getTimestamp() >= before && details.getTimestamp() <= after);
    }

    @Test
    void shouldSerializeStatus_whenMarshalledToJson() throws Exception {
        var details = MillStatusDetails.of(MillStatuses.notFound("chat not found"));
        var json = mapper.readTree(mapper.writeValueAsString(details));
        assertEquals("NOT_FOUND", json.get("status").asText());
    }

    @Test
    void shouldSerializeMessage_whenMarshalledToJson() throws Exception {
        var details = MillStatusDetails.of(MillStatuses.notFound("chat not found"));
        var json = mapper.readTree(mapper.writeValueAsString(details));
        assertEquals("chat not found", json.get("message").asText());
    }

    @Test
    void shouldOmitDetails_whenDetailsIsNull() throws Exception {
        var details = MillStatusDetails.of(MillStatuses.notFound("chat not found"));
        var json = mapper.readTree(mapper.writeValueAsString(details));
        assertFalse(json.has("details"));
    }

    @Test
    void shouldSerializeDetails_whenDetailsProvided() throws Exception {
        var ex = MillStatuses.badRequest("invalid");
        var details = MillStatusDetails.of(ex, Map.of("field", "name"));
        var json = mapper.readTree(mapper.writeValueAsString(details));
        assertEquals("name", json.get("details").get("field").asText());
    }
}
