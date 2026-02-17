package io.qpointz.mill.security.authentication;

import lombok.Getter;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;


public class AuthenticationMethods {

    @Getter
    private final List<? extends AuthenticationMethod> providers;

    private static final Comparator<AuthenticationMethod> authPriorityComparer = Comparator
            .<AuthenticationMethod, Integer>comparing(f-> f.getAuthenticationType().getValue())
            .thenComparing(AuthenticationMethod::getMethodPriority);

    public AuthenticationMethods(List<? extends AuthenticationMethod> providers) {
        this.providers = streamToPrioritizedList(providers.stream());
    }

    public List<? extends AuthenticationMethod> getProviders(AuthenticationType type) {
        return streamToPrioritizedList(providers.stream()
                .filter(f-> f.getAuthenticationType().equals(type)));
    }

    public List<AuthenticationType> getAuthenticationTypes() {
        return providers.stream()
                .map(AuthenticationMethod::getAuthenticationType)
                .distinct()
                .sorted(Comparator.comparing(AuthenticationType::getValue))
                .toList();
    }

    public Collection<AuthenticationMethodDescriptor> getAuthenticationMethodDescriptors() {
        return providers.stream()
                .map(AuthenticationMethod::getDescriptor)
                .distinct()
                .toList();
    }

    public boolean supportsAuthenticationType(AuthenticationType authenticationType) {
        return providers.stream()
                .anyMatch(f-> f.getAuthenticationType() == authenticationType);
    }

    private List<? extends AuthenticationMethod> streamToPrioritizedList(Stream<? extends AuthenticationMethod> stream) {
        return stream.sorted(authPriorityComparer)
                .toList();
    }

}
