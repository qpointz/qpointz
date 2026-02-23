# Data Policy Guide

This document describes the data authorization policy system in Mill. Policies
control which tables, rows, and columns a user can access based on their group
membership.

---

## Overview

Mill policies sit between authentication and query execution. After a user
authenticates, the system resolves their group memberships (via
`SecurityProvider` and `PolicySelector`), selects matching policies, evaluates
actions, and produces instructions for the query rewriter.

**Flow:**

```
User authenticates
  -> SecurityProvider returns principal + authorities
  -> PolicySelector maps authorities to policy names
  -> PolicyActionResolver matches policy actions against queried tables
  -> Query rewriter applies row filters, column restrictions, or table blocks
```

Policies are defined as named collections of actions. Each action specifies a
verb (ALLOW or DENY), an action type, a target table, and optional parameters
(expression, columns, exclusive flag).

---

## Core Concepts

### Policy

A named collection of actions, mapped to a group or role. The policy name
corresponds to a group/authority returned by `SecurityProvider.authorities()`.

### Action

A single rule within a policy. Every action has:

- **Verb** — `ALLOW` or `DENY`. Always required, always explicit on the action.
  There is no policy-level default verb.
- **Type** — what the action controls (`table-access`, `row-filter`, or
  `column-access`).
- **Table** — the target table reference, supporting wildcard patterns.

### Action Types

| Type | Scope | Description |
|------|-------|-------------|
| `table-access` | Table | Allow or deny access to an entire table |
| `row-filter` | Row | Filter visible rows by an expression |
| `column-access` | Column | Restrict which columns are visible |

### Verb

`ALLOW` or `DENY`. Always on the action itself, never implicit or inherited
from the policy.

---

## Policy Format Reference

### YAML Structure

```yaml
policies:
  - name: "analysts"
    actions:
      - verb: ALLOW
        type: table-access
        table: "SCHEMA.TABLE"

      - verb: ALLOW
        type: table-access
        table: "SALES.*"

      - verb: DENY
        type: table-access
        table: "HR.SALARY"

      - verb: ALLOW
        type: row-filter
        table: "SCHEMA.TABLE"
        expression: "department = 'analytics'"

      - verb: ALLOW
        type: row-filter
        table: "SCHEMA.TABLE"
        expression:
          and:
            - eq: ["department", "analytics"]
            - not:
                eq: ["status", "archived"]

      - verb: ALLOW
        type: column-access
        table: "SCHEMA.TABLE"
        include: [col1, col2, col3]

      - verb: DENY
        type: column-access
        table: "SALES.CLIENT"
        exclude: [pii_*]

      - verb: ALLOW
        type: row-filter
        table: "SALES.TRANSACTIONS"
        expression: "country = 'US'"
        exclusive: true

  - name: "anonymous"
    actions:
      - verb: DENY
        type: table-access
        table: "SCHEMA.SENSITIVE_TABLE"
```

### Action Fields

#### `table-access`

| Field | Required | Description |
|-------|----------|-------------|
| `verb` | Yes | `ALLOW` or `DENY` |
| `type` | Yes | `table-access` |
| `table` | Yes | Table reference (supports wildcards) |

#### `row-filter`

| Field | Required | Description |
|-------|----------|-------------|
| `verb` | Yes | `ALLOW` or `DENY` |
| `type` | Yes | `row-filter` |
| `table` | Yes | Table reference (supports wildcards) |
| `expression` | Yes | SQL string or structured expression AST |
| `exclusive` | No | `true` for data partitioning; default `false` |

#### `column-access`

| Field | Required | Description |
|-------|----------|-------------|
| `verb` | Yes | `ALLOW` or `DENY` |
| `type` | Yes | `column-access` |
| `table` | Yes | Table reference (supports wildcards) |
| `include` | Conditional | Columns to include (mutually exclusive with `exclude`) |
| `exclude` | Conditional | Columns to exclude (mutually exclusive with `include`) |

### Wildcard Syntax

Table and column references support glob-style `*` wildcards:

- `*` matches any sequence of characters within a single name segment.
- Matching is case-insensitive.
- No recursive `**` patterns.
- Table references are split by `.` and compared segment-by-segment.

| Pattern | Matches |
|---------|---------|
| `SALES.*` | All tables in the SALES schema |
| `*.AUDIT_*` | All tables starting with `AUDIT_` in any schema |
| `SALES.CLIENT` | Exact table `SALES.CLIENT` |
| `pii_*` (column) | All columns starting with `pii_` |
| `*_id` (column) | All columns ending with `_id` |

### Structured Expression Format

The expression AST is a tree of typed nodes:

```
ExpressionNode (sealed interface)
├── LiteralNode          — constant value (string, number, boolean, null)
├── FieldRefNode         — column reference by name or index
├── CallNode             — function/operator call with operands
│   ├── operator: String — operator or function name
│   └── operands: List<ExpressionNode>
├── CastNode             — CAST(expr AS type)
├── NullCheckNode        — IS NULL / IS NOT NULL
└── RawExpressionNode    — raw SQL string (parsed lazily)
```

**Built-in operators:** `eq`, `ne`, `gt`, `ge`, `lt`, `le`, `and`, `or`,
`not`, `like`, `in`, `between`.

All operators are represented as `CallNode` with an operator name string.

#### Examples

Simple equality:

```yaml
expression:
  eq: ["department", "analytics"]
```

Compound:

```yaml
expression:
  and:
    - eq: ["status", "ACTIVE"]
    - or:
        - eq: ["region", "US"]
        - eq: ["region", "EU"]
```

Function call:

```yaml
expression:
  call:
    function: "UPPER"
    args:
      - field: "name"
```

Between:

```yaml
expression:
  between:
    field: "age"
    low: 18
    high: 65
```

### Dual-Mode Expression

The `expression` field accepts either form:

```yaml
# String literal — parsed at evaluation time
expression: "department = 'analytics'"

# Structured — already parsed, directly buildable via RexBuilder
expression:
  eq: ["department", "analytics"]
```

String literals are stored as `RawExpressionNode` and parsed lazily. Structured
expressions are deserialized into the AST immediately.

---

## Evaluation Mechanics

### Policy Selection

1. `SecurityProvider` returns the user's authorities/groups.
2. `PolicySelector.selectPolicies(policySet)` filters the full set of policy
   names against the user's groups. The default implementation
   (`GrantedAuthoritiesPolicySelector`) returns policies whose name matches a
   user authority (with optional authority remapping).

### How ALLOW and DENY Interact

- **`table-access` DENY** for a member → table is blocked.
- **`table-access` ALLOW** for a non-member → no effect (non-members don't
  receive ALLOW).
- **`row-filter`** for a member → expression applied as a filter.
- **`row-filter`** for a non-member, non-exclusive → no effect.
- **`row-filter`** for a non-member, exclusive → negated expression applied.
- **`column-access` EXCLUDE** for a member → listed columns are hidden.
- **`column-access` INCLUDE** for a member → only listed columns are visible.

### Exclusive Row Filters

When `exclusive: true` on a `row-filter` action:

- **Member** of the policy: filter expression applied as-is.
- **Non-member**: negated expression applied (`NOT (expression)`).

This implements data partitioning by role. For example, a policy with
`country = 'US'` and `exclusive: true` means:

- Members see only US rows (`country = 'US'`).
- Non-members see only non-US rows (`NOT (country = 'US')`).

When `exclusive: false` (default): expression applied for members, nothing
for non-members.

### Wildcard Resolution

`PolicyMatcher` resolves wildcard patterns against concrete table/column names:

1. Table patterns are split by `.` into segments.
2. Each segment is compared case-insensitively with `*` expansion.
3. Segment counts must match (no recursive matching).

### Row-Filter Combination

Multiple `row-filter` actions matching the same table produce multiple
`ResolvedRowFilter` entries. The caller (query rewriter) decides how to combine
them (typically AND for same-policy filters, OR across policies).

### Column-Access Resolution

- `EXCLUDE` with wildcards: each concrete column is tested against each
  pattern. Matching columns are hidden.
- `INCLUDE`: only columns matching at least one pattern are visible.

### Default Behavior

When no policy matches a table: access is **ALLOWED** by default (no
restrictions). This is the "open by default" model. To restrict by default,
add a catch-all DENY policy for anonymous/unauthenticated users.

### Conflicting ALLOW/DENY

When both ALLOW and DENY `table-access` actions match the same table from
policies the user is a member of, **DENY wins** — the table is blocked.

---

## What-If Evaluation

The `PolicyActionResolver.evaluate()` method supports dry-run evaluation:

```java
var result = resolver.evaluate(
    List.of("analysts", "compliance"),   // user's groups
    List.of("SALES", "CLIENT"),          // target table
    List.of("client_id", "name", "pii_ssn", "region")  // requested columns
);
```

Returns `PolicyEvaluationResult` with:

- **`TableResult.access`** — `ALLOWED` or `DENIED`.
- **`TableResult.rowFilters`** — list of `RowFilterResult` with policy name,
  verb, and expression.
- **`TableResult.columns`** — per-column `ColumnResult` with access decision
  and the policy name that decided it.

### Use Cases

- **Debugging**: "Why can't user X see column Y?" → check `ColumnResult`
  for the column, see which policy denied it.
- **Admin preview**: "What would this policy do to group Z?" → run evaluate
  with the group and inspect results.
- **Testing**: assert evaluation results in unit tests without running SQL.
- **Audit**: log what policies were applied per request.

---

## Examples

### Analytics Team with Row and Column Restrictions

```yaml
policies:
  - name: "analysts"
    actions:
      - verb: ALLOW
        type: table-access
        table: "SALES.*"
      - verb: ALLOW
        type: row-filter
        table: "SALES.TRANSACTIONS"
        expression: "region = 'EMEA'"
      - verb: DENY
        type: column-access
        table: "SALES.CLIENT"
        exclude: [pii_*]
```

Analysts can access all SALES tables, see only EMEA transactions, and cannot
see PII columns in the CLIENT table.

### Anonymous Access with Table-Level Deny

```yaml
policies:
  - name: "anonymous"
    actions:
      - verb: DENY
        type: table-access
        table: "HR.*"
      - verb: DENY
        type: table-access
        table: "FINANCE.PAYROLL"
```

Anonymous users cannot access any HR table or the FINANCE.PAYROLL table.

### Wildcard Schema-Wide Policies

```yaml
policies:
  - name: "auditors"
    actions:
      - verb: ALLOW
        type: table-access
        table: "*.AUDIT_*"
      - verb: ALLOW
        type: row-filter
        table: "*.AUDIT_*"
        expression: "created_at > CURRENT_DATE - INTERVAL '90' DAY"
```

Auditors can access any table matching `AUDIT_*` in any schema, but only
see records from the last 90 days.

### Mixed ALLOW/DENY in a Single Policy

```yaml
policies:
  - name: "compliance"
    actions:
      - verb: ALLOW
        type: table-access
        table: "SALES.*"
      - verb: DENY
        type: table-access
        table: "SALES.INTERNAL_NOTES"
      - verb: DENY
        type: column-access
        table: "SALES.CLIENT"
        exclude: [pii_*, secret_*]
```

Compliance team can access all SALES tables except INTERNAL_NOTES, and
cannot see PII or secret columns in CLIENT.

### Structured Expression with Nested Logic

```yaml
policies:
  - name: "regional_managers"
    actions:
      - verb: ALLOW
        type: row-filter
        table: "SALES.ORDERS"
        expression:
          and:
            - or:
                - eq: ["region", "US"]
                - eq: ["region", "CA"]
            - gt: ["amount", 1000]
            - not:
                eq: ["status", "CANCELLED"]
```

Regional managers see US/CA orders over $1000 that are not cancelled.

---

## Import/Export

### Exporting Policies

```java
var exporter = new JsonPolicyExporter();
var out = new FileOutputStream("policies.json");
exporter.export(policyRepository.policies(), out);
```

### Importing Policies

```java
var importer = new YamlPolicyImporter();
var policies = importer.importPolicies(new FileInputStream("policies.yaml"));
```

### Cross-Format Migration

```java
// YAML -> JSON
var yamlImporter = new YamlPolicyImporter();
var jsonExporter = new JsonPolicyExporter();

var policies = yamlImporter.importPolicies(yamlInput);
jsonExporter.export(policies, jsonOutput);
```

Supported formats: JSON, YAML. The domain model's Jackson-compatible design
ensures lossless roundtrip across formats.

---

## Troubleshooting

### Missing Verb

Every action must have `verb: ALLOW` or `verb: DENY`. Omitting it causes a
deserialization error at load time. Check the YAML for actions without a `verb`
field.

### Unknown Action Type

Only `table-access`, `row-filter`, and `column-access` are recognized. Unknown
types are ignored during resolution. Check for typos in the `type` field.

### Wildcard Mismatch

Wildcards match within a single segment only. `SALES.*` matches
`SALES.CLIENT` but not `SALES.PUBLIC.CLIENT` (segment count differs). Verify
the number of segments in both the pattern and the actual table name.

### Access Denied Diagnosis

Use what-if evaluation to diagnose:

```java
var result = resolver.evaluate(
    userGroups, targetTable, requestedColumns);

for (var table : result.tables()) {
    System.out.println("Table: " + table.table() + " -> " + table.access());
    for (var filter : table.rowFilters()) {
        System.out.println("  Row filter from " + filter.policyName()
            + ": " + filter.rawExpression());
    }
    for (var col : table.columns()) {
        if (col.access() == AccessDecision.DENIED) {
            System.out.println("  Column " + col.columnName()
                + " DENIED by " + col.policyName());
        }
    }
}
```

### Verifying Policy Loading

Enable debug logging for `io.qpointz.mill.security.authorization.policy` to
see which policies are loaded and how they resolve. Use the import/export
functionality to serialize the loaded policy set and inspect it.

---

## Domain Model Summary

All policy domain classes live in `core/mill-security` — pure Java, no Spring,
no JPA, no storage-specific dependencies.

| Class | Package | Description |
|-------|---------|-------------|
| `Policy` | `model` | Named collection of actions |
| `PolicyActionEntry` | `model` | Single action: verb + type + table + params |
| `PolicySet` | `model` | Collection of policies |
| `ActionVerb` | (root) | `ALLOW` / `DENY` enum |
| `ActionType` | `model` | Constants: `table-access`, `row-filter`, `column-access` |
| `ColumnsMode` | `model` | `INCLUDE` / `EXCLUDE` enum |
| `AccessDecision` | `model` | `ALLOWED` / `DENIED` enum |
| `ExpressionNode` | `expression` | Sealed interface for expression AST |
| `PolicyMatcher` | `matcher` | Wildcard matching for tables and columns |
| `PolicyActionResolver` | `resolver` | Resolves actions for a table; bridge for rewriter |
| `ResolvedActions` | `resolver` | Resolution result: access, filters, columns |
| `PolicyEvaluationResult` | `resolver` | What-if evaluation result |
| `PolicyImporter` | `io` | Import policies from stream |
| `PolicyExporter` | `io` | Export policies to stream |
