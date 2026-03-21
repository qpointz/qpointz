package io.qpointz.mill.security.authorization.policy.model;

import lombok.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicySet implements Serializable {

    @Builder.Default
    private List<Policy> policies = List.of();

    public static PolicySet of(Collection<Policy> policies) {
        return new PolicySet(List.copyOf(policies));
    }

    public static PolicySet of(Policy... policies) {
        return new PolicySet(List.of(policies));
    }
}
