# Service Discovery

Mill servers expose a discovery endpoint at `/.well-known/mill` that advertises
available services, authentication methods, and schemas.

---

## Current Status

Service discovery is currently a **stub** in mill-py. The client can fetch the
discovery document, but automatic protocol selection based on it is not yet
implemented.

Calling `connect()` with a bare hostname (no scheme) will attempt discovery
and raise `NotImplementedError`:

```python
from mill import connect

# This fetches /.well-known/mill but raises NotImplementedError
client = connect("my-mill-server.example.com")
# NotImplementedError: Service discovery mode is not implemented yet.
# Use an explicit URL: grpc://host:port or http://host:port/path
```

**Recommendation**: Always use an explicit URL scheme (`grpc://`, `http://`,
etc.) until automatic discovery is implemented.

---

## Discovery Document

The `/.well-known/mill` endpoint returns a JSON document like:

```json
{
  "services": [
    { "stereotype": "grpc" },
    { "stereotype": "jet-http" }
  ],
  "security": {
    "enabled": true,
    "authMethods": [
      { "authType": "BASIC" },
      { "authType": "OAUTH2" }
    ]
  },
  "schemas": {
    "skymill": {
      "name": "skymill",
      "link": "http://localhost:8080/.well-known/mill/schemas/skymill"
    }
  }
}
```

---

## Fetching Manually

You can fetch and inspect the discovery document directly:

```python
from mill.discovery import fetch_descriptor

descriptor = fetch_descriptor("localhost", port=8080)

# Available services
for svc in descriptor.services:
    print(f"Service: {svc.stereotype} at {svc.url}")

# Security configuration
print(f"Security enabled: {descriptor.security.enabled}")
print(f"Auth methods: {descriptor.security.auth_methods}")

# Available schemas
for name, schema in descriptor.schemas.items():
    print(f"Schema: {name} -> {schema.link}")
```

---

## Model Classes

| Class | Description |
|-------|-------------|
| `MillServiceDescriptor` | Top-level discovery document |
| `MillServiceEndpoint` | A single service (stereotype + URL) |
| `MillSecurityDescriptor` | Security config (enabled, auth methods) |
| `MillSchemaLink` | Schema name + link |

All classes are frozen dataclasses defined in `mill.discovery`.
