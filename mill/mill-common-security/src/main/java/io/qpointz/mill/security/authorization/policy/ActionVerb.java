package io.qpointz.mill.security.authorization.policy;

public enum ActionVerb {
    ALLOW("allow"),
    DENY("deny");

    private final String verb;

    ActionVerb(String verb) {
        this.verb = verb;
    }

    @Override
    public String toString() {
        return this.verb;
    }
}
