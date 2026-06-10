# WI-201 — mill-py: platform HTTP integration tests

Status: `planned`  
Type: `🧪 test`  
Area: `client`  
Story: [`STORY.md`](STORY.md)

## Goal

Add **`@pytest.mark.integration`** tests under [`clients/mill-py/tests/integration`](../../../../clients/mill-py/tests/integration) that exercise **real HTTP** against a running Mill instance (metadata + schema explorer), complementing unit tests from **WI-199**.

**Coverage targets (minimum):**

1. **Schema explorer** — e.g. `GET /api/v1/schema/context` or list schemas/tree with default profile; assert JSON parses and non-error status.
2. **Metadata** — e.g. `GET /api/v1/metadata/scopes` (read-only); optionally create facet/entity in a dedicated test scope if the environment allows cleanup or uses throwaway data.
3. **Auth** — if the test stack enables security, one test with `BasicAuth` / `BearerToken` per [`clients/mill-py` conventions](../../../../clients/mill-py/tests/integration/conftest.py).

**Harness:**

- **Align with existing [`conftest.py`](../../../../clients/mill-py/tests/integration/conftest.py):** integration config already uses **`MILL_IT_*`** (host, port, TLS, auth, etc.) with **defaults** so `pytest -m integration` can run locally without extra env.
- **Platform HTTP origin:** extend `IntegrationConfig` (or a sibling fixture) with an optional **`MILL_IT_PLATFORM_ORIGIN`** (full origin for Mill REST root, e.g. `http://localhost:8080`). When **unset**, tests that need **`/api/v1/metadata`** or **`/api/v1/schema`** **skip** with a clear reason (do **not** introduce a conflicting `MILL_HTTP_URL` requirement). When set, reuse **`MILL_IT_*`** auth/TLS where applicable for httpx calls to that origin.
- Document new vars in the **`conftest.py`** module docstring and in [`clients/mill-py/README.md`](../../../../clients/mill-py/README.md) alongside existing **`MILL_IT_*`** tables.

## Dependencies

- **WI-192**–**WI-198** (clients implemented); **WI-199** README section helps but can land in parallel.

## Out of scope

- CI wiring in this WI unless already trivial (e.g. documented manual run only); follow-up backlog item for GitLab job is acceptable if noted in the WI closure.

## Acceptance

- `pytest -m integration` runs **existing** data-plane tests unchanged; new platform tests run when **`MILL_IT_PLATFORM_ORIGIN`** is set and **skip** otherwise (clear skip message).
- No flakiness from hard-coded ports without env override.
