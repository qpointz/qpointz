package io.qpointz.mill.security.authorization.policy;

import lombok.*;

import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PolicyActionDescriptor {

    @Getter
    @Setter
    private String policy;

    @Getter
    @Setter
    private ActionVerb verb;

    @Getter
    @Setter
    private String action;

    @Getter
    @Setter
    @Builder.Default
    private Map<String,Object> params = Map.of();

}
