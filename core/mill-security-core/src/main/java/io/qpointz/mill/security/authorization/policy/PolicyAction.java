package io.qpointz.mill.security.authorization.policy;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
public final class PolicyAction {

    @Getter
    private final String policy;

    @Getter
    private final ActionVerb verb;

    @Getter
    private final Action action;

    public List<String> qualifiedId() {
        val res = new ArrayList<String>(List.of(this.getPolicy(), this.getVerb().toString(), this.getAction().actionName()));
        res.addAll(this.getAction().subject());
        return res;
    }

}