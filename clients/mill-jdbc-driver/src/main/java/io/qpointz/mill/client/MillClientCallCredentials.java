package io.qpointz.mill.client;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import lombok.AllArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executor;

@AllArgsConstructor
public class MillClientCallCredentials extends CallCredentials {

    private final Metadata metadata;

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
        applier.apply(this.metadata);
    }

    public static MillClientCallCredentials basicCredentials(String username, String password) {
        final var headerValue = basicAuthHeaderValue(username, password);
        return withAuthorizationHeader(headerValue);
    }

    @NotNull
    public static String basicAuthHeaderValue(String username, String password) {
        val authBytes = String.format("%s:%s", username, password)
                .getBytes(StandardCharsets.UTF_8);
        val authEncoded = Base64.getEncoder().encodeToString(authBytes);
        val headerValue = String.format("Basic %s", authEncoded);
        return headerValue;
    }

    public static String bearerTokenHeaderValue(String token) {
        return String.format("Bearer %s", token);
    }

    public static MillClientCallCredentials bearerTokenCredentials(String token) {
        return withAuthorizationHeader(bearerTokenHeaderValue(token));
    }

    private static MillClientCallCredentials withAuthorizationHeader(String headerValue) {
        val metadata = new Metadata();
        metadata.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), headerValue);
        return new MillClientCallCredentials(metadata);
    }


}
