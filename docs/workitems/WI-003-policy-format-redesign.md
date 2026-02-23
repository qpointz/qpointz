# WI-003: Data Policy Format Redesign

**Type:** refactoring / feature
**Priority:** high
**Rules:** See [RULES.md](RULES.md)
**Branch name:** `refactor/wi-003-policy-format-redesign`
**Depends on:** WI-002 (security module split)

---

## Goal

Redesign the data authorization policy model with:

1. Clear, descriptive action naming.
2. Structured expression format (serializable AST that maps to Calcite RexNode).
3. Wildcard matchers — utility to test table/column names against patterns, returning
   `boolean` match result.
4. Storage-agnostic policy repository interface — not tied to JPA or any specific storage
   technology. Could be backed by RDBMS, document store, NoSQL, or flat files.
5. Import/export capability — policies can be serialized and transferred between
   repository implementations (e.g. import YAML file into a DB-backed repo and vice versa).
6. Domain objects designed as pure Java (no Spring, no JPA, no storage-specific dependency
   in the core module).

This is preparation for persistent policy storage. The specific storage technology is
**not decided yet** — the repository interface must remain abstract enough to support
relational DB, document store, or any other backend. Import/export between repositories
ensures portability.

## Related Backlog Items

- P-15: Resolve duplicate mill.security.authorization.policy prefix across modules
- P-20: Add rate limiting, audit logging, and policy testing framework

## Current Problems

### Misleading names

| Current | Problem |
|---------|---------|
| `expression-filter` | Sounds like it filters expressions; actually filters table rows |
| `rel-read` | Cryptic; `rel` is an internal Substrait term |
| `params: Map<String, Object>` | Untyped bag; not DB-friendly |

### Verb on wrong level

`ActionVerb` (ALLOW/DENY) is **only** on the policy level. A single group that needs
both ALLOW and DENY actions requires duplicate policy entries with the same name but
different verbs. The verb belongs on the **action** level — always required, no
fallback, no ambiguity.

### Expression is string-only

```yaml
expression: "department = 'analytics'"
```

Cannot be indexed, validated, or introspected without parsing. Not suitable for
programmatic construction or DB storage.

### Model not persistence-ready / not serializable

`PolicyActionDescriptor` uses `Map<String, Object> params` — too loose for structured
storage. No default constructors, no stable field structure. Cannot be reliably
serialized/deserialized across formats. No import/export capability.

## Target Policy Model

### Action Types

| Action type | Scope | Description |
|-------------|-------|-------------|
| `table-access` | Table | Allow or deny access to an entire table |
| `row-filter` | Row | Filter visible rows by expression |
| `column-access` | Column | Restrict which columns are visible |

### Wildcard Table and Column References

The `table` field in `table-access`, `row-filter`, and `column-access` actions supports
basic glob-style wildcards (`*`). Column lists in `column-access` also support wildcards.
This keeps policy definitions concise when rules apply to many objects in a schema.

**Rules:**
- `*` matches any sequence of characters within a single name segment.
- Matching is case-insensitive.
- No recursive/`**` patterns — keep it simple.

**Examples:**

| Pattern | Matches |
|---------|---------|
| `SALES.*` | All tables in the SALES schema |
| `*.AUDIT_*` | All tables starting with `AUDIT_` in any schema |
| `SALES.CLIENT` | Exact table `SALES.CLIENT` (no wildcard) |
| `pii_*` (column) | All columns starting with `pii_` |

The matcher splits the reference by `.` and compares each segment against the
corresponding segment of the actual object name using basic glob. Implementation should
be a small utility in `core/mill-security` — no regex, just `*` expansion.

### YAML Format

Verb (ALLOW/DENY) is **always** on the action level. Required on every action, no
policy-level default, no fallback. Missing verb fails at load time.

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

      # Exclusive row-filter — members see US rows, non-members see non-US rows
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

### Exclusive Row Filters

The `exclusive` flag on `row-filter` actions implements data partitioning by role.
When `exclusive: true`:

- **Policy applies** (user is a member): filter expression applied as-is
  (`country = 'US'` -> user sees US rows).
- **Policy does not apply** (user is not a member): **negated** expression applied
  (`country <> 'US'` -> user sees non-US rows).

When `exclusive: false` (default): standard behavior — expression applied for members,
nothing happens for non-members.

This replaces the existing `isNegate` flag on `ExpressionFilterAction` with a clearer
name and well-defined semantics. The `PolicyActionResolver` handles the negation —
the rewriter always receives a ready-to-apply expression.

### Structured Expression AST

The expression tree is a serializable DAG that maps to Calcite `RexNode` via `RexBuilder`.
It must be:

- **Traversable** — visitor/walker pattern
- **Extensible** — new operators and functions added without changing core tree
- **RexNode-compatible** — operators available in Rex
- **JSON/YAML serializable** — for CLOB storage in DB
- **Supports function calls** — arbitrary SQL functions

#### Node Types

```
ExpressionNode (sealed interface)
├── LiteralNode          — constant value (string, number, boolean, null)
├── FieldRefNode         — column reference by name or index
├── CallNode             — function/operator call with operands
│   ├── operator: String — operator or function name
│   └── operands: List<ExpressionNode>
├── CastNode             — CAST(expr AS type)
└── NullCheckNode        — IS NULL / IS NOT NULL
```

#### Built-in Operators (extensible)

**Comparison:** `eq`, `ne`, `gt`, `ge`, `lt`, `le`
**Logical:** `and`, `or`, `not`
**Arithmetic:** `add`, `sub`, `mul`, `div`, `mod`
**String:** `like`, `similar`
**Null:** `is-null`, `is-not-null`
**Collection:** `in`, `between`
**Function call:** `call` with function name + args

All operators are represented as `CallNode` with an operator name string. This keeps the
tree uniform and extensible — adding a new operator requires no new node type.

#### Structured Expression Examples

Simple equality:
```yaml
eq: ["department", "analytics"]
# Shorthand for: CallNode(op="eq", operands=[FieldRef("department"), Literal("analytics")])
```

Compound:
```yaml
and:
  - eq: ["status", "ACTIVE"]
  - or:
      - eq: ["region", "US"]
      - eq: ["region", "EU"]
```

Function call:
```yaml
call:
  function: "UPPER"
  args:
    - field: "name"
```

Between:
```yaml
between:
  field: "age"
  low: 18
  high: 65
```

#### Dual-Mode Expression

The `expression` field accepts either form:

```yaml
# String literal — parsed at evaluation time
expression: "department = 'analytics'"

# Structured — already parsed, directly buildable via RexBuilder
expression:
  eq: ["department", "analytics"]
```

The policy loader detects the form (string vs map) and produces the appropriate
`ExpressionNode`. String literals are stored as-is and parsed lazily. Structured
expressions are deserialized into the AST immediately.

## Domain Model (Storage-Agnostic)

All classes in `core/mill-security` — pure Java, no Spring, no JPA, no storage-specific
dependencies. Designed to be serializable to any format (JSON, YAML, protobuf, etc.)
and persistable to any storage backend.

### Design Rules

- Default no-arg constructor (can be `protected`)
- Getter/setter pairs for all fields
- `Serializable` where appropriate
- Stable field types: `String`, `Long`, `Enum`, `List<>`
- Expression trees serializable as JSON string
- No framework annotations in the core module
- No assumptions about storage technology (no JPA entity design, no document IDs, etc.)

### Wildcard Matcher

A small utility class in `core/mill-security`:

```java
public class PolicyMatcher {
    // Test if a concrete table name matches a pattern
    boolean matchesTable(List<String> pattern, List<String> tableName);

    // Test if a concrete column name matches a pattern
    boolean matchesColumn(String pattern, String columnName);
}
```

- Splits by `.`, compares segment-by-segment.
- `*` matches any sequence of characters within one segment.
- Case-insensitive.
- Returns `true`/`false`.
- Used by `PolicyEvaluatorImpl` to resolve wildcard subjects against concrete names.

### Policy Domain Structure

```
Policy
  name: String                    — role/group name (e.g. "analysts")
  actions: List<PolicyAction>

PolicyAction
  verb: ActionVerb                — ALLOW or DENY (required, no fallback)
  type: String                    — "table-access", "row-filter", "column-access"
  table: List<String>             — table reference (may contain wildcards)
  expression: ExpressionNode      — for row-filter (null for others)
  exclusive: Boolean              — for row-filter only (default false)
  columns: List<String>           — for column-access (null for others)
  columnsMode: ColumnsMode        — INCLUDE or EXCLUDE (null for non column-access)
```

Every action is self-describing. No implicit behavior, no ambiguity.

### Policy Evaluation Result ("What-If")

The evaluator should support a dry-run / what-if mode: given a user context (groups)
and a query target (table, columns), return a structured result explaining what the
policy layer would do — without executing anything.

```java
public record PolicyEvaluationResult(
    List<TableResult> tables
) {
    public record TableResult(
        List<String> table,
        AccessDecision access,           // ALLOWED, DENIED
        List<RowFilterResult> rowFilters,
        List<ColumnResult> columns
    ) {}

    public record RowFilterResult(
        String policyName,
        ActionVerb verb,
        ExpressionNode expression        // the filter that would be applied
    ) {}

    public record ColumnResult(
        String columnName,
        AccessDecision access,           // ALLOWED, DENIED
        String policyName                // which policy decided this
    ) {}

    public enum AccessDecision { ALLOWED, DENIED }
}
```

**Usage:**

```java
val result = evaluator.evaluate(
    List.of("analysts", "compliance"),   // user's groups
    List.of("SALES", "CLIENT"),          // target table
    List.of("client_id", "name", "pii_ssn", "region")  // requested columns
);

// result.tables[0].access == ALLOWED
// result.tables[0].rowFilters == [ RowFilterResult("analysts", ALLOW, eq("region","EMEA")) ]
// result.tables[0].columns == [
//   ColumnResult("client_id", ALLOWED, "analysts"),
//   ColumnResult("name", ALLOWED, "analysts"),
//   ColumnResult("pii_ssn", DENIED, "compliance"),   // matched pii_* exclude
//   ColumnResult("region", ALLOWED, "analysts")
// ]
```

This enables:
- **Debugging** — "why can't user X see column Y?"
- **Admin preview** — "what would this policy do to group Z?"
- **Testing** — assert evaluation results in unit tests without running SQL.
- **Audit** — log what policies were applied per request.

The evaluator returns the result as a pure data structure. It does not touch the
rewrite layer — the caller (rewriter, admin UI, test) decides what to do with it.

### Action Resolver (for Plan Rewriter)

A class that accepts an object name and returns the resolved actions to apply.
This is the bridge between the policy layer and the plan rewriter — the rewriter
doesn't need to understand policy selection, wildcard matching, or evaluation
internals. It just asks: "what actions apply to this table?"

```java
public class PolicyActionResolver {

    private final PolicyEvaluator evaluator;
    private final PolicyMatcher matcher;

    // Given a concrete table name, return all matching actions
    // (already filtered by selected policies and wildcard-resolved)
    ResolvedActions resolve(List<String> tableName);
}

public record ResolvedActions(
    List<String> table,
    AccessDecision tableAccess,                // ALLOWED or DENIED
    List<ResolvedRowFilter> rowFilters,         // row-filter expressions to apply
    ResolvedColumnAccess columnAccess           // column restrictions (may be null)
) {
    public record ResolvedRowFilter(
        ActionVerb verb,
        ExpressionNode expression,       // already negated if exclusive and non-member
        boolean negated                  // true if expression was negated (for diagnostics)
    ) {}

    public record ResolvedColumnAccess(
        ColumnsMode mode,                       // INCLUDE or EXCLUDE
        List<String> columns                    // resolved column names (wildcards expanded)
    ) {}

    public enum AccessDecision { ALLOWED, DENIED }

    public boolean isDenied() { return tableAccess == AccessDecision.DENIED; }
    public boolean hasRowFilters() { return rowFilters != null && !rowFilters.isEmpty(); }
    public boolean hasColumnRestrictions() { return columnAccess != null; }
}
```

**Usage by the rewriter:**

```java
// In the plan rewriter (RelShuttleImpl.visit(TableScan))
val resolved = actionResolver.resolve(scan.getTable().getQualifiedName());

if (resolved.isDenied()) {
    return LogicalValues.createEmpty(scan);     // block access
}

RelNode result = scan;

if (resolved.hasRowFilters()) {
    // build LogicalFilter from resolved.rowFilters()
}

if (resolved.hasColumnRestrictions()) {
    // build LogicalProject from resolved.columnAccess()
}

return result;
```

Key points:
- The rewriter only depends on `PolicyActionResolver` and `ResolvedActions` — no
  direct dependency on `PolicyRepository`, `PolicySelector`, or `PolicyMatcher`.
- Wildcard expansion happens inside the resolver, not the rewriter.
- `ResolvedActions` is a pure immutable data object — easy to test, mock, and serialize.
- The resolver is also used by the what-if evaluation (shares the same resolution logic).

### Policy Selector Interface

```java
public interface PolicySelector {
    Set<String> selectPolicies(Set<String> policySet);
}
```

Simplified from the current `selectPolicies(ActionVerb verb, Set<String> policySet)` —
verb is no longer a selector concern. The selector returns matching policy names based
on the current security context (authorities/groups). Verb filtering happens in the
evaluator when iterating actions.

### Policy Repository Interface

```java
public interface PolicyRepository {
    Collection<Policy> policies();
}
```

Storage-agnostic — any backend implements this interface. The interface may evolve
(e.g. add query methods, pagination) as storage requirements become clearer.
Requirements are **intentionally minimal** at this stage.

Known future implementations (not in this WI):
- `InMemoryPolicyRepository` — already exists (needs update), used for tests and YAML config.
- RDBMS-backed (JPA or plain JDBC)
- Document store (MongoDB, etc.)
- File-based (YAML/JSON files on disk)

### Import/Export

Policies must be transferable between repository implementations:

```java
public interface PolicyExporter {
    void export(Collection<Policy> policies, OutputStream target);
}

public interface PolicyImporter {
    Collection<Policy> importFrom(InputStream source);
}
```

Supported formats (at minimum): JSON, YAML.

Use case examples:
- Export policies from YAML config -> import into DB-backed repo.
- Export policies from DB repo -> write to YAML file for version control.
- Migrate between storage backends.

The domain model's serialization design (Jackson-compatible, stable field types)
makes this straightforward. Import/export operates on `Policy` collections —
the common currency across all repository implementations.

### Conceptual Storage Structures (for reference, not implemented in this WI)

**Relational (if RDBMS chosen later):**

```
policy(id, name, description)
policy_action(id, policy_id, verb, type, table_ref, expression, expression_type, columns, columns_mode)
policy_assignment(id, policy_id, scope_type, scope_value)
```

`verb` is on `policy_action` only. No verb on `policy`.

**Document (if NoSQL chosen later):**

```json
{
  "name": "analysts",
  "actions": [
    { "verb": "ALLOW", "type": "row-filter", "table": "SALES.*", "expression": { "eq": ["region", "EMEA"] } },
    { "verb": "DENY", "type": "table-access", "table": "HR.SALARY" },
    { "verb": "DENY", "type": "column-access", "table": "SALES.CLIENT", "exclude": ["pii_*"] }
  ]
}
```

Both are possible because the domain model is storage-agnostic.

## Scope of This Work Item

### In scope

1. Rename action types: `expression-filter` -> `row-filter`, `rel-read` -> `table-access`.
2. Add `column-access` action type.
3. Move `ActionVerb` to action level (required, no fallback). Remove from policy level.
4. Simplify `PolicySelector` — remove verb parameter (verb is now on action, not selector concern).
5. Define `ExpressionNode` sealed interface and node types in `core/mill-security`.
6. Implement expression tree serialization/deserialization (Jackson JSON/YAML).
7. Support dual-mode expression (string literal or structured).
8. Redesign `PolicyActionDescriptor` into typed, storage-agnostic domain classes
   (`Policy` + `PolicyAction`).
9. Define storage-agnostic `PolicyRepository` interface (minimal, intentionally open).
10. Implement `PolicyMatcher` — wildcard matching utility for table and column references.
11. Implement `PolicyImporter` / `PolicyExporter` interfaces + JSON/YAML implementations.
12. Update `PolicyEvaluatorImpl` to use new model and wildcard-aware matching via `PolicyMatcher`.
13. Implement `PolicyActionResolver` — accepts object name, returns `ResolvedActions`
    (table access decision, row filters, column restrictions). Bridge for plan rewriter.
14. Implement what-if evaluation — `evaluate(groups, table, columns)` returning
    `PolicyEvaluationResult` with access decisions, applied filters, and per-column status.
14. Update YAML config parsing to support new format.
15. Maintain backward compatibility with existing string-literal expressions.
16. Write detailed user documentation on policy mechanics (`docs/design/platform/policy-guide.md`).
17. Create example integration test using `BackendContextRunner` demonstrating full policy
    application pipeline (for user to extend with rewrite validation).

### Out of scope

- **Plan rewrite changes** — `TableFacetVisitor`, `TableFacetPlanRewriter`,
  `TableFacetFactoryImpl`, `RecordFacet`, `TableFacet`, `AttributeFacet` — user will
  update these separately.
- Expression tree to `RexNode` conversion via `RexBuilder` — part of rewrite layer.
- Concrete storage-backed PolicyRepository implementations (future WI).
- Policy admin UI (future WI).
- Migration tooling for existing policy configs (manual for now).

## User Documentation

Deliverable: `docs/design/platform/policy-guide.md` — a standalone reference for anyone
authoring, debugging, or administering data policies. **Must be written as part of this
work item** — not a stub, not an outline, but complete, detailed, production-quality
documentation covering every feature implemented in this WI.

### Required Content

**1. Overview**
- What policies are and what they protect (tables, rows, columns).
- Relationship between policies, groups/roles, and the query pipeline.
- High-level flow: user authenticates -> groups resolved -> policies selected ->
  actions evaluated -> query rewritten.

**2. Core Concepts**
- **Policy** — a named collection of actions, mapped to a group/role.
- **Action** — a single rule: verb (ALLOW/DENY) + type + target.
- **Verb** — ALLOW or DENY, always on the action (never implicit).
- **Action types** — `table-access`, `row-filter`, `column-access` with description
  of what each one does and when it applies.

**3. Policy Format Reference**
- Full YAML format with annotated examples.
- Each action type documented with all fields, required vs optional.
- Wildcard syntax for table and column references (rules, examples, limitations).
- Structured expression format (AST nodes, operators, function calls, between, in).
- Dual-mode expressions — string literal vs structured, when to use which.

**4. Evaluation Mechanics**
- How policies are selected for a user (selector, group matching, remap).
- How ALLOW and DENY interact — evaluation order, precedence rules.
- How wildcard table/column patterns are matched against concrete names.
- How row-filter expressions from multiple policies combine (AND/OR logic).
- How column-access include/exclude lists are resolved.
- What happens when no policy matches (default behavior).
- What happens when conflicting ALLOW/DENY exist for the same target.

**5. What-If Evaluation**
- How to use the what-if API to preview policy effects.
- Reading the `PolicyEvaluationResult` — table access, row filters, column decisions.
- Using what-if for debugging ("why can't user X see column Y?").

**6. Examples**
- Complete example: analytics team with row-level and column-level restrictions.
- Complete example: anonymous/public access with table-level deny.
- Complete example: wildcard-based schema-wide policies.
- Complete example: mixed ALLOW/DENY within a single policy.
- Complete example: structured expression with nested AND/OR/function calls.

**7. Import/Export**
- Exporting policies to YAML/JSON.
- Importing policies from file to repository.
- Cross-format migration.

**8. Troubleshooting**
- Common errors: missing verb, unknown action type, wildcard mismatch.
- How to diagnose "access denied" using what-if evaluation.
- How to verify policies are loaded correctly (logging, what-if).

## Test Coverage Plan

Scope: policy model, matchers, selector, evaluator, import/export. SQL rewrite layer
(`TableFacetFactory`, `TableFacetPlanRewriter`, `TableFacetVisitor`, `RecordFacet`,
`TableFacet`, `AttributeFacet`) is out of scope — user will update and test separately.

### Current Test Inventory

#### `core/mill-security` — `PolicyEvaluatorImplTest` (4 tests)

- `selectAction` — select by policy + verb, check qualifiedId
- `getListByType` — filter by action type + subject
- `getByVerbType` — filter by verb + type + subject
- `getActionsSet` — actionsBy with policy set + subject

Uses `InMemoryPolicyRepository` with synthetic action types. `PolicySelector` is always mocked.

#### `data/mill-data-backend-core` — `GrantedAuthoritiesPolicySelectorTest` (3 tests)

- `trivia` — ALLOW selects matching, DENY selects non-matching (verb-based — will change)
- `failsOnUnknown` — null verb throws (verb-based — will change)
- `authRemap` — authority name remapping

Note: selector interface changes in this WI (verb removed from selector, moved to action).
Existing tests will be rewritten.

### Gaps (classes with no or insufficient tests)

| Class / Area | Module | Gap |
|-------------|--------|-----|
| `PolicyActionDescriptorRepository` | core/mill-security | No tests — `fromDescriptor()`, `rel-filter` mapping, error cases |
| `ExpressionFilterAction.fromDescriptor()` | core/mill-security | No tests — param extraction, validation, error messages |
| `ExpressionFilterAction.isEmpty()` | core/mill-security | No tests — null/empty expression detection |
| `TableReadAction` | core/mill-security | No tests — action name, subject, equality |
| `PolicyAction.qualifiedId()` | core/mill-security | Only indirectly tested in evaluator |
| `InMemoryPolicyRepository` | core/mill-security | Only used as fixture, no direct tests |
| `PolicyEvaluatorImpl` — empty results | core/mill-security | No tests for no-match/empty-repo paths |
| `PolicyEvaluatorImpl` — selector delegation | core/mill-security | Not verified that selector receives correct policy set |
| `GrantedAuthoritiesPolicySelector` — edge cases | data/mill-data-backend-core | Empty authorities, null remap, all-match/no-match |

### Tests to Add

#### A. `core/mill-security` — Existing Code

**`PolicyActionDescriptorRepositoryTest`:**
- valid rel-filter descriptor -> correct PolicyAction
- null/empty/unknown action -> IllegalArgumentException
- multiple descriptors parsed correctly
- empty descriptors list -> empty actions

**`ExpressionFilterActionTest`:**
- fromDescriptor with valid params (compound & simple table name)
- missing/empty/blank name -> exception
- missing/empty/blank expression -> exception
- actionName() returns "rel-filter"
- subject() returns tableName
- isEmpty() for null, empty, and non-empty expressions
- equality and negate flag distinction

**`TableReadActionTest`:**
- actionName() returns "rel-read"
- subject() returns tableName
- equality by table name
- builder pattern works

**`PolicyActionTest`:**
- qualifiedId format for ALLOW and DENY (verb now on action)
- multi-part subject in qualifiedId
- equality/inequality by verb and type
- getter access

**`InMemoryPolicyRepositoryTest`:**
- returns all actions from builder
- empty list -> empty actions
- preserves order and mixed action types

**Extend `PolicyEvaluatorImplTest`:**
- empty when no policies match selector
- empty when no actions match subject or type
- multiple policies with same subject
- empty repository returns empty
- selector receives full policy name set (ArgumentCaptor)
- verb filtering at action level (ALLOW-only, DENY-only, mixed within one policy)
- single policy with mixed ALLOW/DENY actions returns correct subset for each verb

**`PolicyActionResolverTest`:**
- exact table match -> returns matching actions
- wildcard table match `["SALES","*"]` -> resolves for `["SALES","CLIENT"]`
- no matching actions -> returns `ALLOWED` with empty filters and no column restrictions
- table denied -> `tableAccess == DENIED`
- row-filter actions -> returned as `ResolvedRowFilter` with verb and expression
- multiple row-filters from different policies -> all returned
- column-access exclude -> `ResolvedColumnAccess(EXCLUDE, [columns])`
- column-access include -> `ResolvedColumnAccess(INCLUDE, [columns])`
- column wildcard expanded -> `pii_*` resolved against concrete column list
- mixed actions for same table -> table access + row filters + column restrictions all present
- exclusive row-filter, member -> expression as-is, `negated == false`
- exclusive row-filter, non-member -> expression negated, `negated == true`
- non-exclusive row-filter, non-member -> not included in results

**`PolicyEvaluationResultTest`** (what-if):
- table allowed -> `access == ALLOWED`, no row filters
- table denied -> `access == DENIED`
- table with row-filter -> `rowFilters` contains expression from matching policy
- table with multiple row-filters from different policies -> all listed
- column allowed -> `access == ALLOWED` with source policy name
- column denied by wildcard exclude (`pii_*`) -> `access == DENIED` with policy name
- column not in any column-access policy -> default ALLOWED
- mixed: table allowed, some columns denied, row filters applied
- no matching policies -> all tables allowed, no filters, all columns allowed
- wildcard table pattern matches -> result includes the matched table

**Extend `GrantedAuthoritiesPolicySelectorTest`:**
- simplified selector (no verb parameter): returns matching policy names
- empty authorities -> no policies selected
- empty policy set -> empty result
- null remap map handled gracefully
- multiple remaps
- all-match and no-match scenarios

#### B. New WI-003 Code (after redesign)

**`RowFilterActionTest`:** string expression, structured expression, JSON roundtrip
**`TableAccessActionTest`:** action name, serialization
**`ColumnAccessActionTest`:** include/exclude modes, serialization
**`ExpressionNodeTest`:** all node types (Literal, FieldRef, Call, Cast, NullCheck), deep nesting, roundtrip

**`PolicyMatcherTest`:**
- `matchesTable`: exact match `["SALES","CLIENT"]` vs `["SALES","CLIENT"]` -> true
- `matchesTable`: no match `["SALES","CLIENT"]` vs `["SALES","ORDERS"]` -> false
- `matchesTable`: trailing wildcard `["SALES","*"]` vs `["SALES","CLIENT"]` -> true
- `matchesTable`: trailing wildcard `["SALES","*"]` vs `["SALES","ORDERS"]` -> true
- `matchesTable`: leading wildcard `["*","CLIENT"]` vs `["SALES","CLIENT"]` -> true
- `matchesTable`: mid-segment wildcard `["SALES","AUDIT_*"]` vs `["SALES","AUDIT_LOG"]` -> true
- `matchesTable`: case insensitivity `["sales","*"]` vs `["SALES","CLIENT"]` -> true
- `matchesTable`: segment count mismatch `["SALES","*"]` vs `["SALES","PUBLIC","T"]` -> false
- `matchesTable`: single segment `["orders"]` vs `["orders"]` -> true
- `matchesColumn`: `"pii_*"` vs `"pii_ssn"` -> true
- `matchesColumn`: `"pii_*"` vs `"name"` -> false
- `matchesColumn`: exact `"ssn"` vs `"ssn"` -> true
- `matchesColumn`: case insensitivity `"PII_*"` vs `"pii_dob"` -> true

**`PolicyImporterExporterTest`:**
- JSON roundtrip: export policies to JSON -> import from JSON -> equals original
- YAML roundtrip: export policies to YAML -> import from YAML -> equals original
- cross-format: export to YAML -> import -> export to JSON -> import -> equals original
- import with all action types (table-access, row-filter, column-access)
- import with structured expression preserves AST structure
- import with wildcard table refs preserves patterns
- export empty policy collection -> valid empty document
- import malformed input -> meaningful error

#### C. Plain JUnit Integration Test — Full Policy-to-Rewrite Chain

A plain JUnit test (no Spring Boot, no application context) in
`data/mill-data-backend-core` that wires the full chain end-to-end:
policy definition -> selector -> evaluator -> facet factory -> plan rewrite.

**Everything is defined programmatically** — policies, backend schema, and security
context. No external files, no Spring context.

Validation is done by **introspecting the rewritten Calcite `RelNode` tree**, not by
comparing SQL strings or executing queries. Substrait is deprecated and subject to
removal (see `docs/design/platform/substrait-to-relnode-migration.md`); the rewriter
will operate on Calcite types after Phase 3 of that migration.

The ported rewriter (Phase 3, step 3.4) wraps `TableScan` nodes with `LogicalFilter`
via `RelShuttleImpl`. Assertions inspect the `RelNode` tree using Calcite types:
`LogicalFilter`, `LogicalTableScan`, `LogicalProject`, `RexCall`, `RexInputRef`, etc.

**`PolicyApplicationExampleTest`** (in `data/mill-data-backend-core/src/test/java/.../rewriters/`):

```java
@Slf4j
class PolicyApplicationExampleTest {

    // Schema defined inline — only DDL, no seed data (we inspect the plan, not results)
    static final String H2_INIT = String.join("\\;",
        "CREATE SCHEMA IF NOT EXISTS ts",
        "CREATE TABLE ts.clients (client_id INT, name VARCHAR, region VARCHAR, segment VARCHAR)",
        "CREATE TABLE ts.orders (order_id INT, client_id INT, amount DECIMAL, status VARCHAR)"
    );

    static final JdbcBackendContextRunner runner = JdbcBackendContextRunner.jdbcH2Context(
            "jdbc:h2:mem:policy-app;INIT=" + H2_INIT, "ts");

    // --- programmatic policy definitions (verb always on action) ---

    static PolicyRepository rowFilterPolicies() {
        return InMemoryPolicyRepository.of(List.of(
            Policy.builder().name("analysts").actions(List.of(
                PolicyAction.builder()
                    .verb(ALLOW).type("row-filter")
                    .table(List.of("ts", "clients"))
                    .expression("region = 'EMEA'").build(),
                PolicyAction.builder()
                    .verb(ALLOW).type("row-filter")
                    .table(List.of("ts", "orders"))
                    .expression("status = 'ACTIVE'").build(),
                PolicyAction.builder()
                    .verb(DENY).type("table-access")
                    .table(List.of("ts", "secrets")).build()
            )).build()
        ));
    }

    // --- programmatic security stubs ---

    static SecurityProvider securityProvider(String principal, String... authorities) {
        return new SecurityProvider() {
            public String getPrincipalName() { return principal; }
            public Collection<String> authorities() { return List.of(authorities); }
        };
    }

    static SecurityDispatcher securityDispatcher(SecurityProvider sp) {
        return new SecurityDispatcher() {
            public String principalName() { return sp.getPrincipalName(); }
            public Collection<String> authorities() { return sp.authorities(); }
        };
    }

    // --- helper: parse SQL -> RelNode, apply policy rewriter, return rewritten RelNode ---

    RelNode rewriteWithPolicies(BackendContextRunner ctx, PolicyRepository repo,
                                SecurityDispatcher dispatcher, String sql) {
        val selector = new GrantedAuthoritiesPolicySelector(dispatcher, Map.of());
        val evaluator = new PolicyEvaluatorImpl(repo, selector);
        val factory = new TableFacetFactoryImpl(
                evaluator, dispatcher,
                ctx.getSchemaProvider(), ctx.getSqlProvider());

        // Parse SQL to Calcite RelNode (post-migration SqlProvider.parseSql returns QueryPlan)
        val parseResult = ctx.getSqlProvider().parseSql(sql);
        assertTrue(parseResult.isSuccess());
        val sourceRelNode = parseResult.getQueryPlan().relNode();

        // Apply rewriter (post-migration: operates on RelNode via RelShuttleImpl)
        val rewriter = new TableFacetPlanRewriter(
                TableFacetFactories.fromCollection(factory.facets()));
        return rewriter.rewritePlan(sourceRelNode);
    }

    // --- Calcite RelNode tree introspection ---

    @Test
    void shouldWrapMatchingTableInLogicalFilter() {
        runner.run(ctx -> {
            val sp = securityProvider("alice", "analysts");
            val dispatcher = securityDispatcher(sp);
            val rewritten = rewriteWithPolicies(ctx, rowFilterPolicies(), dispatcher,
                    "SELECT * FROM `ts`.`clients`");

            // Rewritten tree: LogicalFilter -> LogicalTableScan("ts","clients")
            assertInstanceOf(LogicalFilter.class, rewritten);
            val filter = (LogicalFilter) rewritten;
            assertInstanceOf(LogicalTableScan.class, filter.getInput());
            val scan = (LogicalTableScan) filter.getInput();
            assertEquals(List.of("ts", "clients"), scan.getTable().getQualifiedName());

            // Filter condition is a RexCall (e.g. =($2, 'EMEA'))
            assertInstanceOf(RexCall.class, filter.getCondition());
            val call = (RexCall) filter.getCondition();
            assertEquals(SqlKind.EQUALS, call.getKind());
        });
    }

    @Test
    void shouldNotWrapTableWithoutMatchingPolicy() {
        runner.run(ctx -> {
            val sp = securityProvider("alice", "other_group");
            val dispatcher = securityDispatcher(sp);
            val rewritten = rewriteWithPolicies(ctx, rowFilterPolicies(), dispatcher,
                    "SELECT * FROM `ts`.`clients`");

            // No matching policy -> no LogicalFilter, just TableScan
            assertInstanceOf(LogicalTableScan.class, rewritten);
        });
    }

    @Test
    void shouldApplyTableAccessDenyPolicy() {
        // anonymous user -> DENY table-access
        // Introspect: root is LogicalValues.createEmpty() or similar empty rel
    }

    @Test
    void shouldApplyColumnAccessPolicy() {
        // column-access with exclude list
        // Introspect: LogicalProject wrapping TableScan, check projected field indices
        // via project.getProjects() — list of RexInputRef with expected indices
    }

    @Test
    void shouldApplyWildcardTablePolicy() {
        // row-filter on "ts.*" -> both clients and orders wrapped in LogicalFilter
    }
}
```

Key design decisions:
- **Plain JUnit** — no `@SpringBootTest`, no application context, no DI container.
  Everything wired manually via constructors.
- **DDL-only schema** — only table structure needed (no seed data). We inspect the
  rewritten plan, not query results. Schema defined inline via H2 `INIT=`.
- **Calcite plan introspection** — assertions walk the Calcite `RelNode` tree using
  `LogicalFilter`, `LogicalTableScan`, `LogicalProject`, `RexCall`, `RexInputRef`,
  `SqlKind`, etc. No Substrait types. No fragile SQL string comparison.
- **`SqlProvider.parseSql()`** — post Substrait-to-RelNode migration, returns `QueryPlan`
  holding a `RelNode`. SQL is parsed programmatically, no external plan files.
- **Inline policies** — `InMemoryPolicyRepository` with `PolicyAction` lists.
- **Stub security** — anonymous `SecurityProvider` / `SecurityDispatcher` implementations.
- **Helper method** `rewriteWithPolicies()` — encapsulates the full chain so each test
  only varies policies, security context, and SQL input.
- Two tests fully implemented (`shouldWrapMatchingTableInLogicalFilter`,
  `shouldNotWrapTableWithoutMatchingPolicy`). Remaining stubs describe the expected
  Calcite `RelNode` shape to assert.

### Coverage Targets

- `core/mill-security/.../authorization/policy/` >= 80%
- `GrantedAuthoritiesPolicySelector` >= 90%

## Verification

1. All existing policy tests pass with renamed action types.
2. New tests for structured expression parsing and serialization roundtrip.
3. New tests for `column-access` action type.
4. `PolicyMatcher` tests pass — all table and column wildcard patterns.
5. Import/export roundtrip tests pass (JSON, YAML, cross-format).
6. Evaluator correctly resolves wildcard subjects via `PolicyMatcher`.
7. `PolicyActionResolver` correctly resolves actions for concrete table names including
   wildcards; rewriter can consume `ResolvedActions` without policy internals.
8. What-if evaluation returns correct access decisions, filters, and per-column status.
8. Example integration test (`PolicyApplicationExampleTest`) passes — wires full chain
   via `BackendContextRunner`, validates facets are produced for each action type.
8. Test coverage meets targets listed above.
9. `core/mill-security` has **zero** Spring/JPA/storage-specific dependencies.
10. `./gradlew test` passes in all affected modules.
11. User documentation (`docs/design/platform/policy-guide.md`) covers all action types,
    wildcard syntax, evaluation mechanics, what-if, import/export, and examples.
12. Plan rewrite layer (`TableFacetVisitor`, `TableFacetPlanRewriter`, etc.) is **not
    modified** — user updates separately.

## Estimated Effort

Large — new expression AST, action type redesign, matcher, import/export, test suite.
No plan rewrite changes.
