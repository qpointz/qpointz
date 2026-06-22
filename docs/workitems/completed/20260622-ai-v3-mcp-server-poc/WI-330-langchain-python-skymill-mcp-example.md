# WI-330 — LangChain Python + Skymill MCP Integration Example

Status: `done`  
Type: `📝 docs` / `✨ feature`  
Area: `ai`, `examples`  
Backlog refs: **A-56** (demonstration)  
Depends on: [WI-329](WI-329-mill-ai-mcp-transport-http.md)  
**Included in story scope** for MR — manual LangChain smoke only  
**Packaging:** Poetry example under `misc/examples/ai-mcp-langchain-skymill/`

## Problem Statement

The MCP stack proves protocol wiring, but does not yet show **external agent integration** with a
popular stack against real Mill data. A minimal **LangChain (Python)** agent calling Mill MCP tools
over **Streamable HTTP** validates the capabilities-first story.

## Goal

Deliver a **documented example**: Python LangChain agent → **HTTP MCP on mill-service** →
`schema-exploration` capabilities backed by the Skymill dataset configured on the server
(`skymill-ai` profile).

## Demonstration scenario

A simple ReAct-style agent answers natural-language questions about Skymill Airlines data using MCP
tools only (no Mill REST chat API):

| Example user prompt | Expected MCP tool usage |
|---------------------|-------------------------|
| "What schemas and tables exist in Skymill?" | `schema.list_schemas`, `schema.list_tables` |
| "What columns does the bookings table have?" | `schema.list_columns` |
| "How are bookings related to passengers?" | `schema.list_relations` |
| "Validate SQL that counts bookings" (stretch) | `sql-query.validate_sql` with `data-analysis` profile on server |

Reference analytical prompts from [`test/datasets/skymill/README.md`](../../../../test/datasets/skymill/README.md).

## In Scope

### 1. mill-service as MCP backend (not local Skymill bundling)

Document running **mill-service** with:

- Profile: `skymill-ai` (Skymill flow + canonical metadata seeds on server classpath)
- `mill.ai.mcp.enabled=true`
- `mill.ai.mcp.profile=schema-exploration` (default demo) or `data-analysis` for SQL stretch
- `mill.security.enable` per environment; example shows auth env vars for HTTP client

No embedded data-plane launcher in the example — data lives on the server.

### 2. Python example package

Location: **`misc/examples/ai-mcp-langchain-skymill/`**

```
misc/examples/ai-mcp-langchain-skymill/
  README.md           # prerequisites, mill-service start, env vars, run steps
  pyproject.toml
  mill_mcp_config.py  # MILL_MCP_URL + auth headers
  skymill_agent.py    # minimal LangChain agent (streamable_http)
  smoke_tools.py      # tools/list smoke without LLM
  .env.example
```

Stack:

- `langchain` + `langchain-openai`
- `langchain-mcp-adapters` — MCP client with **`transport: streamable_http`** to `MILL_MCP_URL`

### 3. Documentation

- README architecture diagram: Python → HTTP MCP → mill-service → capabilities → Skymill
- Link from [`v3-mcp-capability-exposure.md`](../../../design/agentic/v3-mcp-capability-exposure.md) §10
- Document mill-service startup (`skymill-ai`, MCP flags, security)
- **Manual smoke checklist** (not CI-gated): 2–3 example prompts; no secrets in repo

### 4. Optional lightweight automation

- `smoke_tools.py`: verify `tools/list` includes `schema.list_tables` via LangChain MCP client (no LLM)

## Out of Scope

- Production deployment of Python agent
- mill-ui integration
- PyPI package
- CI with live OpenAI or full mill-service boot in pipeline
- SQL execution over MCP (validate only)
- Local capability execution without mill-service
- stdio bridge (backlog A-96 / WI-328) — optional for stdio-only IDE clients; not required for this demo

## Acceptance Criteria

- Documented steps: start mill-service (Skymill + MCP) → run Python agent → agent calls
  **`schema.list_tables`** and **`schema.list_columns`** via **HTTP MCP**.
- Example uses **only** Mill MCP tools (no parallel HTTP tool definitions in Python).
- Skymill dataset / `skymill-ai` profile documented.

## Deliverables

- `misc/examples/ai-mcp-langchain-skymill/` with README + `skymill_agent.py` + `smoke_tools.py`
- mill-service MCP + Skymill runbook in README
- Design doc cross-link

## Verify (manual)

```bash
# Terminal A — mill-service backend
./gradlew :apps:mill-service:bootRun \
  --args='--spring.profiles.active=skymill-ai --mill.ai.mcp.enabled=true --mill.ai.mcp.profile=schema-exploration'

# Terminal B — smoke (no LLM)
cd misc/examples/ai-mcp-langchain-skymill
poetry install
export MILL_MCP_URL=http://localhost:8080/services/mcp
poetry run python smoke_tools.py

# Terminal B — LangChain agent
export OPENAI_API_KEY=sk-...
poetry run python skymill_agent.py "List tables in the skymill schema"
```

Expected: agent response references Skymill tables after MCP tool calls over HTTP.
