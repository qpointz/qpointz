package io.qpointz.mill.data.backend.access.http.components;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.proto.*;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.function.Function;

public class MessageHelper {

    private final static String PROTOBUFX_MEDIA_VALUE = "application/x-protobuf";

    private final static String PROTOBUF_MEDIA_VALUE = "application/protobuf";

    private final static MediaType PROTOBUF_MEDIA_TYPE = MediaType.parseMediaType(PROTOBUFX_MEDIA_VALUE);

    private MessageHelper() {
        // Utility class, no instantiation
    }

    public static <T extends Message> T fromJson(String json, Message.Builder builder) {
        try {
            val content = json == null || json.isEmpty() || json.isBlank() ? "{}" : json;
            JsonFormat.parser()
                    .merge(content, builder);
            return (T) builder.build();
        } catch (IOException e) {
            throw new MillRuntimeException("Failed to parse JSON to Protobuf message", e);
        }
    }

    public static String asJson(Message message) {
        try {
            return JsonFormat.printer()
                    .print(message);
        } catch (IOException e) {
            throw new MillRuntimeException("Failed to convert Protobuf message to JSON", e);
        }
    }

    public static byte[] asProtobuf(Message message) {
        return message.toByteArray();
    }

    public static ResponseEntity<?> asResponseEntity(Message message, MediaType acceptHeader) {
        if (acceptHeader != null && MediaType.APPLICATION_PROTOBUF.equalsTypeAndSubtype(acceptHeader)) {
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_PROTOBUF)
                    .body(asProtobuf(message));
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(asJson(message));

    }


    public static <Request extends Message, Response extends Message> ResponseEntity<?> apply(
            byte[] payload,
            Function<byte[],Request> parse,
            Function<Request,Response> apply,
            Message.Builder builder,
            String contentTypeHeader,
            String acceptsHeader) {
        val contentType = MediaType.parseMediaType(contentTypeHeader);
        Request request;

        if (contentType.equalsTypeAndSubtype(MediaType.APPLICATION_PROTOBUF)) {
            request = parse.apply(payload);
        } else {
            request = fromJson(new String(payload), builder);
        }
        Response response = apply.apply(request);

        val accepts = MediaType.parseMediaType(acceptsHeader);
        return asResponseEntity(response, accepts);
    }


    public static ListSchemasRequest listSchemasRequest(byte[] payload) {
        try {
            return ListSchemasRequest.parseFrom(payload);
        } catch (IOException e) {
            throw new MillRuntimeException("Failed to parse HandshakeResponse from JSON", e);
        }
    }

    public static HandshakeRequest handshakeRequest(byte[] payload) {
        try {
            return HandshakeRequest.parseFrom(payload);
        } catch (IOException e) {
            throw new MillRuntimeException("Failed to parse HandshakeResponse from JSON", e);
        }
    }

    public static GetSchemaRequest getSchemaRequest(byte[] payload) {
        try {
            return GetSchemaRequest.parseFrom(payload);
        } catch (IOException e) {
            throw new MillRuntimeException("Failed to parse HandshakeResponse from JSON", e);
        }
    }

    public static ParseSqlRequest parseSqlRequest(byte[] payload) {
        try {
            return ParseSqlRequest.parseFrom(payload);
        } catch (IOException e) {
            throw new MillRuntimeException("Failed to parse HandshakeResponse from JSON", e);
        }
    }

    public static QueryRequest queryRequest(byte[] payload) {
        try {
            return QueryRequest.parseFrom(payload);
        } catch (IOException e) {
            throw new MillRuntimeException("Failed to parse HandshakeResponse from JSON", e);
        }
    }

    public static QueryResultRequest queryResultRequest(byte[] payload) {
        try {
            return QueryResultRequest.parseFrom(payload);
        } catch (IOException e) {
            throw new MillRuntimeException("Failed to parse HandshakeResponse from JSON", e);
        }
    }
}
