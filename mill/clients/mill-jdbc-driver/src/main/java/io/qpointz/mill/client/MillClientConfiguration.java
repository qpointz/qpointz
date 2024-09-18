package io.qpointz.mill.client;

import io.qpointz.mill.MillConnection;
import lombok.*;

import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class MillClientConfiguration {

    public static final String HOST_PROP = "host";
    public static final String PORT_PROP = "port";

    public static final String USERNAME_PROP = "user";
    public static final String PASSWORD_PROP = "password";

    public static final String BEARER_TOKEN_PROP = "bearerToken";

    public static final String TLS_KEY_CERT_CHAIN_PROP = "tlsKeyCertChain";
    public static final String TLS_KEY_PRIVATE_KEY_PROP = "tlsKeyPrivateKey";
    public static final String TLS_KEY_PRIVATE_KEY_PASSWORD_PROP = "tlsKeyPrivateKeyPassword";
    public static final String TLS_TRUST_ROOT_CERT_PROP = "tlsTrustRootCert";

    public static final String CLIENT_CHANNEL_PROP = "clientChannel";
    public static final String CLIENT_CHANNEL_GRPC_VALUE = "grpc";
    public static final String CLIENT_CHANNEL_INPROC_VALUE = "in-proc";

    @Getter
    @Builder.Default
    private final String host = "localhost";

    @Getter
    @Builder.Default
    private int port = 9099;

    @Getter
    @Builder.Default
    private String username = null;

    @Getter
    @Builder.Default
    private String bearerToken = null;

    @Getter
    @Builder.Default
    private String password = null;

    @Getter
    @Builder.Default
    private String tlsKeyCertChain = null;

    @Getter
    @Builder.Default
    private String tlsKeyPrivateKey = null;

    @Getter
    @Builder.Default
    private String tlsKeyPrivateKeyPassword = null;

    @Getter
    @Builder.Default
    private String tlsTrustRootCert = null;

    @Getter
    @Builder.Default
    private String clientChannel = CLIENT_CHANNEL_GRPC_VALUE;


    public static MillClientConfigurationBuilder builder() {
        return new MillClientConfigurationBuilder();
    }

    public static MillClientConfigurationBuilder builder(String url) {
        return builder().url(url);
    }

    public boolean requiresTls() {
        return this.tlsKeyCertChain != null || this.tlsKeyPrivateKey != null || this.tlsKeyPrivateKeyPassword != null || this.tlsTrustRootCert != null;
    }

    public boolean requiresBasicAuth() {
        return this.username != null || this.password != null;
    }

    public boolean requiresBearerAuth() {
        return this.bearerToken != null;
    }

    public static class MillClientConfigurationBuilder {

        public MillClient buildClient() {
            return MillClient.fromConfig(this);
        }

        public MillClientConfigurationBuilder fromProperties(Properties properties) {
            return this
                    .stringProp(properties, HOST_PROP, null, this::host)
                    .anyProp(properties, PORT_PROP, 9099, Integer::parseInt, this::port)
                    .stringProp(properties, USERNAME_PROP, null, this::username)
                    .stringProp(properties, PASSWORD_PROP, null, this::password)
                    .stringProp(properties, BEARER_TOKEN_PROP, null, this::bearerToken)
                    .stringProp(properties, TLS_KEY_CERT_CHAIN_PROP, null, this::tlsKeyCertChain)
                    .stringProp(properties, TLS_KEY_PRIVATE_KEY_PROP, null, this::tlsKeyPrivateKey)
                    .stringProp(properties, TLS_KEY_PRIVATE_KEY_PASSWORD_PROP, null, this::tlsKeyPrivateKeyPassword)
                    .stringProp(properties, TLS_TRUST_ROOT_CERT_PROP, null, this::tlsTrustRootCert)
                    .stringProp(properties, CLIENT_CHANNEL_PROP, null, this::clientChannel)
                    ;

        }

        public MillClientConfigurationBuilder url(String url) {
            var props = new Properties();
            props = MillUrlParser.apply(url, props);
            return this.fromProperties(props);
        }

        public MillClientConfigurationBuilder basicCredentials(String username, String password) {
            return this.username(username)
                    .password(password);
        }

        private MillClientConfigurationBuilder stringProp(Properties properties, String key, String defaultValue, Consumer<String> consumer ) {
            return anyProp(properties, key, defaultValue, k->k , consumer);
        }

        private <T> MillClientConfigurationBuilder anyProp(Properties properties, String key, T defaultValue, Function<String,T> convert, Consumer<T> consume) {
            if (!properties.containsKey(key)) {
                if (defaultValue != null) {
                    consume.accept(defaultValue);
                }
            } else {
                consume.accept(convert.apply(properties.getProperty(key)));
            }
            return this;
        }
    }

}
