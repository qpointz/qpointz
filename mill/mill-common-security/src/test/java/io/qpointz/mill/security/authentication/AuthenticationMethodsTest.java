package io.qpointz.mill.security.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test-custom-security")
@Import({AuthenticationMethodsTest.TestConfig.class})
class AuthenticationMethodsTest extends BaseTest {

    @Test
    void trivial(@Autowired AuthenticationMethods methods) {
        assertEquals(4, methods.getProviders().size());
    }

    @Test
    void methodsSortedByPriority(@Autowired AuthenticationMethods methods) {
        val providers = methods.getProviders();
        assertEquals("CUSTOM1", ((TestConfig.TestAuthMethod) providers.get(0)).getName());
        assertEquals("TOKEN1", ((TestConfig.TestAuthMethod) providers.get(1)).getName());
        assertEquals("PASSWORD2", ((TestConfig.TestAuthMethod) providers.get(2)).getName());
        assertEquals("PASSWORD1", ((TestConfig.TestAuthMethod) providers.get(3)).getName());
    }

    @Test
    void getMethodByTypeShouldBeSorted(@Autowired AuthenticationMethods allMethods) {
        val providers = allMethods.getProviders(AuthenticationType.BASIC);
        assertEquals(2, providers.size());
        assertEquals("PASSWORD2", ((TestConfig.TestAuthMethod) providers.get(0)).getName());
        assertEquals("PASSWORD1", ((TestConfig.TestAuthMethod) providers.get(1)).getName());
    }

    @Test
    void getTypes(@Autowired AuthenticationMethods allMethods) {
        val types = allMethods.getAuthenticationTypes();
        assertEquals(3, types.size());
        assertEquals(AuthenticationType.CUSTOM, types.get(0));
        assertEquals(AuthenticationType.OAUTH2, types.get(1));
        assertEquals(AuthenticationType.BASIC, types.get(2));
    }

    public static class TestConfig {

        @AllArgsConstructor
        private class TestAuthMethod implements AuthenticationMethod {

            @Getter
            private final AuthenticationType authenticationType;

            @Getter
            private final int methodPriority;

            @Getter
            private final String name;

            @Getter
            private final AuthenticationProvider authenticationProvider = new TestingAuthenticationProvider();

            @Override
            public void applyLoginConfig(HttpSecurity http) throws Exception {
                //no customization
            }

            @Override
            public void applySecurityConfig(HttpSecurity http) throws Exception {
                //no customization
            }

            @Override
            public AuthenticationMethodDescriptor getDescriptor() {
                return new AuthenticationMethodDescriptor() {
                    @Override
                    public AuthenticationType getAuthenticationType() {
                        return AuthenticationType.CUSTOM;
                    }
                };
            }
        }

        @Bean
        public AuthenticationMethod method1() {
            return new TestAuthMethod(AuthenticationType.CUSTOM, 0, "CUSTOM1");
        }

        @Bean
        public AuthenticationMethod method2() {
            return new TestAuthMethod(AuthenticationType.BASIC, 200, "PASSWORD1");
        }

        @Bean
        public AuthenticationMethod method3() {
            return new TestAuthMethod(AuthenticationType.BASIC, 100, "PASSWORD2");
        }

        @Bean
        public AuthenticationMethod method4() {
            return new TestAuthMethod(AuthenticationType.OAUTH2, 1, "TOKEN1");
        }
    }

}
