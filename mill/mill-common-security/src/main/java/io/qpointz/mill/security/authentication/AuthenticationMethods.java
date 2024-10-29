package io.qpointz.mill.security.authentication;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


public class AuthenticationMethods {

    @Getter
    private final List<AuthenticationMethod> providers;

    private static final Comparator<AuthenticationMethod> authPriorityComparer = Comparator
            .<AuthenticationMethod, Integer>comparing(f-> f.getAuthenticationType().getValue())
            .thenComparing(AuthenticationMethod::getMethodPriority);

    public AuthenticationMethods(List<AuthenticationMethod> providers) {
        this.providers = streamToPrioritizedList(providers.stream());
    }

    public List<AuthenticationMethod> getProviders(AuthenticationType type) {
        return streamToPrioritizedList(providers.stream()
                .filter(f-> f.getAuthenticationType().equals(type)));
    }

    public List<AuthenticationType> getAuthenticationTypes() {
        return providers.stream()
                .map(f-> f.getAuthenticationType())
                .distinct()
                .sorted(Comparator.comparing(AuthenticationType::getValue))
                .toList();
    }

    private List<AuthenticationMethod> streamToPrioritizedList(Stream<AuthenticationMethod> stream) {
        return stream.sorted(authPriorityComparer)
                .toList();
    }

}
