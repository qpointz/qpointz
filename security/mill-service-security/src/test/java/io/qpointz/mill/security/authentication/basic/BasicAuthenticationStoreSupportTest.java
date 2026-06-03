package io.qpointz.mill.security.authentication.basic;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class BasicAuthenticationStoreSupportTest {

    @Test
    void shouldTreatStoreAsJpa_whenStoreIsJpa() {
        var env = new MockEnvironment()
                .withProperty(BasicAuthenticationStoreSupport.ENABLE_PROPERTY, "true")
                .withProperty(BasicAuthenticationStoreSupport.STORE_PROPERTY, "jpa");

        assertThat(BasicAuthenticationStoreSupport.isJpaStore(BasicAuthenticationStoreSupport.effectiveStore(env)))
                .isTrue();
        assertThat(BasicAuthenticationStoreSupport.isFileResourceStore(BasicAuthenticationStoreSupport.effectiveStore(env)))
                .isFalse();
    }

    @Test
    void shouldTreatStoreAsFileResource_whenStoreIsClasspath() {
        var env = new MockEnvironment()
                .withProperty(BasicAuthenticationStoreSupport.ENABLE_PROPERTY, "true")
                .withProperty(BasicAuthenticationStoreSupport.STORE_PROPERTY, "classpath:passwd.yml");

        assertThat(BasicAuthenticationStoreSupport.isFileResourceStore(BasicAuthenticationStoreSupport.effectiveStore(env)))
                .isTrue();
        assertThat(BasicAuthenticationStoreSupport.isJpaStore(BasicAuthenticationStoreSupport.effectiveStore(env)))
                .isFalse();
    }

    @Test
    void shouldHonorLegacyFileStore_whenStoreUnset() {
        var env = new MockEnvironment()
                .withProperty(BasicAuthenticationStoreSupport.ENABLE_PROPERTY, "true")
                .withProperty(BasicAuthenticationStoreSupport.LEGACY_FILE_STORE_PROPERTY, "file:./auth.yml");

        assertThat(BasicAuthenticationStoreSupport.effectiveStore(env)).isEqualTo("file:./auth.yml");
        assertThat(BasicAuthenticationStoreSupport.isFileResourceStore(BasicAuthenticationStoreSupport.effectiveStore(env)))
                .isTrue();
    }
}
