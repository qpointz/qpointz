# mill-ai-v3-cli

Interactive playground for Mill AI v3 (manual agent testing). Not a supported product surface.

## Run

```bash
cd ai
OPENAI_API_KEY=sk-... ./gradlew :mill-ai-v3-cli:run --console=plain
```

- `hello` (default): hello-world profile.
- `schema`: schema authoring / exploration profile (`AGENT=schema` or positional `schema`).

Optional: `OPENAI_MODEL`, `OPENAI_BASE_URL`, `SCHEMA_SOURCE` (default `demo`).

The `sql-query` capability is **generate-only**: validate SQL, emit generated-SQL artifacts; execution is **host-side**. For Spring-backed `SqlValidator` wiring see **`mill-ai-v3-autoconfigure`** (`MillAiV3SqlValidatorAutoConfiguration`).
