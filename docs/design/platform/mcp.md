# MCP Data Provider — Specification

## Common Objects

### Error
{
  "error": {
    "code": "STRING",          // QUERY_FAILED | TIMEOUT | UNAUTHORIZED | INVALID_INPUT | RESULT_TRUNCATED | NOT_FOUND | RATE_LIMITED | INTERNAL
    "message": "STRING",
    "hint": "STRING|null",
    "trace_id": "STRING"
  }
}

### TabularResult
{
  "schema": [ { "name":"STRING", "type":"STRING", "nullable":true, "hints": {"semantic":"email|id|latlon"} } ],
  "rows": [ [ any, ... ] ],
  "row_count": 12345 | null,
  "has_more": true | false,
  "page_token": "STRING|null",
  "source": "STRING",
  "truncated": true | false
}

### TableRef
{ "catalog":"STRING", "schema":"STRING", "table":"STRING" }

### TimeRange
{ "from":"ISO-8601|relative|null", "to":"ISO-8601|relative|null", "default_window":"P30D|null" }

### Capabilities
{
  "dialects": ["ansi","calcite","mssql","duckdb"],
  "limits": {
    "default_max_rows": 1000,
    "hard_max_rows": 50000,
    "page_size_bytes": 1048576,
    "timeout_seconds": 30
  },
  "features": {
    "paging": true,
    "explain": true,
    "substrait": true,
    "profiling": true,
    "pii_masking": true,
    "materialization": true,
    "lineage": false
  },
  "security": { "auth":["api_key","oauth2"], "row_level_security":true, "masking":true }
}

## Resources

### res://catalogs
GET →
{ "items":[ { "name":"STRING", "etag":"STRING", "last_modified":"ISO-8601" } ] }

### res://schemas/{catalog}
GET →
{ "items":[ { "catalog":"STRING","schema":"STRING","etag":"STRING" } ] }

### res://tables/{catalog}/{schema}
GET (pattern?, limit?) →
{ "items":[ { "catalog":"C","schema":"S","table":"T","type":"TABLE|VIEW","comment":"..." } ] }

### res://table/{catalog}/{schema}/{table}
GET →
{
  "table": { "catalog":"C","schema":"S","table":"T","type":"TABLE|VIEW","comment":"..." },
  "columns":[ { "name":"N","type":"STRING","nullable":true,"default":null,"comment":"..." } ],
  "constraints": { "primary_key":["..."], "foreign_keys":[{"columns":["..."],"ref": TableRef}] },
  "stats": { "row_count":123456, "size_bytes":987654321, "updated_at":"ISO-8601" },
  "etag":"STRING"
}

### res://samples/{catalog}/{schema}/{table}
GET (method=head|random, limit=100) →
{ "sample": { TabularResult } }

### res://lineage/{catalog}/{schema}/{table}
GET →
{ "upstream":[ TableRef... ], "downstream":[ TableRef... ] }

## Tools

### Metadata

#### list_schemas
Input:
{ "catalog":"STRING|null" }
Output:
{ "items":[ {"catalog":"C","schema":"S"} ] }

#### list_tables
Input:
{ "catalog":"STRING|null", "schema":"STRING|null", "pattern":"STRING|null", "limit":100 }
Output → like res://tables/...

#### get_table_schema
Input:
{ "ref": TableRef }
Output:
{ "schema":[ {"name":"N","type":"STRING","nullable":true,"hints":{}} ] }

#### get_stats
Input:
{ "ref": TableRef, "columns":["..."]|null }
Output:
{ "row_count":123, "columns":{ "COL": { "min":"...","max":"...","null_rate":0.12,"ndv":42 } } }

### Data Access

#### sample_table
Input:
{ "ref": TableRef, "limit":100, "method":"head|random" }
Output → TabularResult (no paging).

#### query_sql
Input:
{
  "sql":"STRING",
  "dialect":"ansi|calcite|mssql|duckdb",
  "max_rows":1000|null,
  "page_token":"STRING|null",
  "time_range": TimeRange|null,
  "dry_run": false
}
Output → TabularResult (with paging).

#### query_plan
Input:
{ "substrait":{...}, "max_rows":1000, "page_token":"STRING|null" }
Output → TabularResult.

#### explain_sql
Input:
{ "sql":"STRING", "dialect":"STRING|null", "format":"text|json" }
Output:
{ "format":"text|json", "explain":"STRING|object", "estimated_rows":12345|null, "estimated_cost":12.34|null }

### Profiling

#### profile_table
Input:
{ "ref": TableRef, "columns":["..."]|null, "bins":20 }
Output:
{
  "summary": { "row_count":123, "null_rate":{ "COL":0.1 } },
  "distributions": { "COL": { "type":"numeric|categorical","bins":[ {"lo":"...","hi":"...","count":42} ] } },
  "topk": { "COL":[ {"value":"...","count":99} ] }
}

#### detect_pii
Input:
{ "ref": TableRef, "columns":["..."]|null, "locale":"auto|en|de" }
Output:
{ "columns": { "EMAIL": {"pii":"email","confidence":0.97} } }

#### preview_masked
Input:
{ "ref": TableRef, "policy_id":"STRING", "limit":50 }
Output → TabularResult with applied masking.

### Materialization

#### materialize_sql
Input:
{
  "sql":"STRING",
  "target": TableRef,
  "mode":"create|replace|append",
  "dialect":"STRING|null"
}
Output:
{ "status":"ok", "affected_rows":12345, "target":TableRef, "duration_ms":1234 }

#### create_view
Input:
{ "sql":"STRING", "name":TableRef, "or_replace":true }
Output:
{ "status":"ok", "view": TableRef }

## Prompts

### ask_for_disambiguation
Table "{table}" was found in schemas {candidates}. Choose one.

### ask_for_time_window
Table "{table}" is large. Specify a time window (e.g. last 30 days) or explicit from/to.

### ask_for_sensitive_columns_ack
Query includes sensitive columns {cols}. Confirm or specify a masking policy.

### ask_for_missing_keys_or_joins
No join key found between {left} and {right}. Provide matching columns or filters.

## Semantic Rules

- Reads (resources, list*, get*, sample, explain) are idempotent.
- Writes (materialize_sql, create_view) must be safe with explicit error codes.
- Pagination: enforce default_max_rows; always return has_more + page_token.
- Unknown sizes: allowed as row_count=null.
- Security: return policy_applied if masking/RLS applied.
- Dialects: explicit dialect; on mismatch return INVALID_INPUT + supported list.
- Observability: include trace_id in all responses.
