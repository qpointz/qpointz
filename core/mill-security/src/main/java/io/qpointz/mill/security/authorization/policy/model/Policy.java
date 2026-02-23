package io.qpointz.mill.security.authorization.policy.model;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy implements Serializable {

    private String name;

    @Builder.Default
    private List<PolicyActionEntry> actions = List.of();
}
