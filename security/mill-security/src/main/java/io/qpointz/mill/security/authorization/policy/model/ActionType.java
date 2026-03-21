package io.qpointz.mill.security.authorization.policy.model;

public final class ActionType {

    private ActionType() {}

    public static final String TABLE_ACCESS = "table-access";
    public static final String ROW_FILTER = "row-filter";
    public static final String COLUMN_ACCESS = "column-access";
}
