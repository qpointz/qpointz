package io.qpointz.mill.security.authentication.basic;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OnBasicAuthenticationStoreConditionsTest {

    private final OnBasicAuthenticationJpaStoreCondition jpaCondition = new OnBasicAuthenticationJpaStoreCondition();
    private final OnBasicAuthenticationFileStoreCondition fileCondition = new OnBasicAuthenticationFileStoreCondition();
    private final AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

    @Test
    void shouldActivateJpaCondition_whenStoreIsJpaAndBasicEnabled() {
        MockEnvironment env = new MockEnvironment()
                .withProperty(BasicAuthenticationStoreSupport.ENABLE_PROPERTY, "true")
                .withProperty(BasicAuthenticationStoreSupport.STORE_PROPERTY, "jpa");

        assertThat(jpaCondition.matches(context(env), metadata)).isTrue();
        assertThat(fileCondition.matches(context(env), metadata)).isFalse();
    }

    @Test
    void shouldActivateFileCondition_whenStoreIsResourceAndBasicEnabled() {
        MockEnvironment env = new MockEnvironment()
                .withProperty(BasicAuthenticationStoreSupport.ENABLE_PROPERTY, "true")
                .withProperty(BasicAuthenticationStoreSupport.STORE_PROPERTY, "file:./config/auth.yml");

        assertThat(fileCondition.matches(context(env), metadata)).isTrue();
        assertThat(jpaCondition.matches(context(env), metadata)).isFalse();
    }

    private static ConditionContext context(MockEnvironment environment) {
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);
        return context;
    }
}
