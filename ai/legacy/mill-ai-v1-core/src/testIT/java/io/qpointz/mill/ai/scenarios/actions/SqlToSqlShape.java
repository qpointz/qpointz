package io.qpointz.mill.ai.scenarios.actions;


import java.util.Set;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SqlToSqlShape {

    private SqlToSqlShape() {
    }

    /**
     * SqlShape is a semantic regression sensor.
     *
     * It intentionally ignores execution plans and SQL AST details.
     * It exists solely to detect semantic regressions
     * in LLM-generated SQL.
     */
    public record SqlShape(
            Set<String> tables,
            boolean hasJoin,
            boolean hasAggregation,
            boolean hasGrouping,
            boolean hasWhere,
            boolean hasOrdering,
            boolean hasLimit,
            boolean hasSubquery,
            int selectArity,
            Set<String> filterColumns,
            Set<String> aggregationFunctions
    ) {}

    private static final Pattern TABLE_PATTERN =
            Pattern.compile("(FROM|JOIN)\\s+([\"`]?\\w+[\"`]?\\.)?[\"`]?([A-Z0-9_]+)[\"`]?",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern AGG_PATTERN =
            Pattern.compile("\\b(COUNT|SUM|AVG|MIN|MAX)\\s*\\(",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern SUBQUERY_PATTERN =
            Pattern.compile("\\(\\s*SELECT\\b",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern SELECT_PATTERN =
            Pattern.compile("SELECT\\s+(.*?)\\s+FROM",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern WHERE_COLUMN_PATTERN =
            Pattern.compile("\\bWHERE\\b(.*?)($|GROUP BY|ORDER BY|LIMIT)",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern COLUMN_PATTERN =
            Pattern.compile("[\"`]?([A-Z0-9_]+)[\"`]?",
                    Pattern.CASE_INSENSITIVE);

    public static SqlShape extract(String sql) {
        if (sql == null || sql.isBlank()) {
            return new SqlShape(
                    Set.of(),
                    false, false, false, false, false, false, false,
                    0,
                    Set.of(),
                    Set.of()
            );
        }

        String normalized = normalize(sql);

        Set<String> tables = new LinkedHashSet<>();
        Set<String> filterColumns = new LinkedHashSet<>();
        Set<String> aggregationFunctions = new LinkedHashSet<>();

        // tables
        Matcher tableMatcher = TABLE_PATTERN.matcher(normalized);
        while (tableMatcher.find()) {
            tables.add(tableMatcher.group(3).toUpperCase());
        }

        // aggregation functions
        Matcher aggMatcher = AGG_PATTERN.matcher(normalized);
        while (aggMatcher.find()) {
            aggregationFunctions.add(aggMatcher.group(1).toUpperCase());
        }

        // select arity
        int selectArity = extractSelectArity(normalized);

        // filter columns
        Matcher whereMatcher = WHERE_COLUMN_PATTERN.matcher(normalized);
        if (whereMatcher.find()) {
            String whereClause = whereMatcher.group(1);
            Matcher colMatcher = COLUMN_PATTERN.matcher(whereClause);
            while (colMatcher.find()) {
                filterColumns.add(colMatcher.group(1).toUpperCase());
            }
        }

        boolean hasJoin = normalized.contains(" JOIN ");
        boolean hasWhere = normalized.contains(" WHERE ");
        boolean hasGrouping = normalized.contains(" GROUP BY ");
        boolean hasOrdering = normalized.contains(" ORDER BY ");
        boolean hasLimit = normalized.contains(" LIMIT ");
        boolean hasAggregation = !aggregationFunctions.isEmpty();
        boolean hasSubquery = SUBQUERY_PATTERN.matcher(normalized).find();

        return new SqlShape(
                tables,
                hasJoin,
                hasAggregation,
                hasGrouping,
                hasWhere,
                hasOrdering,
                hasLimit,
                hasSubquery,
                selectArity,
                filterColumns,
                aggregationFunctions
        );
    }

    private static int extractSelectArity(String sql) {
        Matcher matcher = SELECT_PATTERN.matcher(sql);
        if (!matcher.find()) {
            return 0;
        }

        String selectClause = matcher.group(1).trim();

        // SELECT * case
        if (selectClause.equals("*")) {
            return -1; // explicit marker for SELECT *
        }

        return selectClause.split(",").length;
    }

    private static String normalize(String sql) {
        return sql
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase();
    }
}
