# ibis Gap Matrix (Post WI-025)

This matrix captures the first broad implementation slice and the remaining
correctness work tracked by WI-023.

| Area | Status | Notes / Gap |
|------|--------|-------------|
| Backend connect lifecycle (gRPC/HTTP) | ✅ implemented | `mill.ibis.connect(...)` wraps existing Mill transport stack. |
| Table introspection (`list_tables`, `table`, schema mapping) | ✅ implemented | Backed by Mill `list_schemas`/`get_schema`. |
| Expression compile baseline | ✅ partial | Uses ibis SQL backend compiler defaults (DuckDB-style) for broad coverage. |
| Dialect capability gating | ✅ initial | `WITH`, `INTERSECT`, `EXCEPT`, `LATERAL` checks from dialect flags. |
| Raw SQL interoperability | ✅ implemented | `backend.sql(..., schema=...)` path supported. |
| Read-only constraints | ✅ enforced | DDL (`create/drop table/view`) intentionally unsupported. |
| Function-by-function certification | ❌ pending WI-023 | Requires generated validation report and CI drift detection. |
| Set-op certification across dialects | ❌ pending WI-023 | Integration test currently marked `xfail` to keep gap explicit. |
| Dialect-specific SQL rewrites | ❌ pending WI-023 | Current compiler path does not yet do full dialect rewrite tuning. |

## Follow-up checkpoints for WI-023

1. Generate validation corpus by dialect using the same feature catalog as
   `core/mill-sql` YAML specs.
2. Upgrade gating from conservative SQL keyword checks to certified feature
   coverage.
3. Replace `xfail` integration gaps with hard-pass tests once certified.
