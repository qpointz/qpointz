# Authentication

mill-py supports three authentication modes matching the Mill server's
security configuration.

---

## Anonymous (Default)

When no `auth` parameter is passed, the client connects without credentials.
This works when the server has security disabled.

```python
from mill import connect

client = connect("grpc://localhost:9090")

# Verify identity
resp = client.handshake()
print(resp.authentication.name)  # "ANONYMOUS"
```

---

## Basic Auth

HTTP Basic authentication — username and password are base64-encoded and sent
with each request.

```python
from mill import connect
from mill.auth import BasicAuth

client = connect(
    "grpc://localhost:9090",
    auth=BasicAuth("reader", "secret"),
)

resp = client.handshake()
print(resp.authentication.name)  # "reader"
```

Works with both gRPC and HTTP transports.

---

## Bearer Token

OAuth2 / JWT bearer token authentication.

```python
from mill import connect
from mill.auth import BearerToken

client = connect(
    "grpc://localhost:9090",
    auth=BearerToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."),
)
```

The token is sent as-is in the `Authorization: Bearer <token>` header.
Token acquisition and refresh are the caller's responsibility.

---

## TLS / Mutual-TLS

### Server TLS (verify server certificate)

Use `grpcs://` or `https://` schemes with an optional CA certificate:

```python
# System CA bundle (default)
client = connect("grpcs://secure.host:443")

# Custom CA certificate
client = connect("grpcs://secure.host:443", tls_ca="/path/to/ca.pem")
```

### Mutual TLS (client certificate)

Provide client certificate and private key for mTLS:

```python
client = connect(
    "grpcs://mtls.host:443",
    tls_ca="/path/to/ca.pem",
    tls_cert="/path/to/client.pem",
    tls_key="/path/to/client-key.pem",
)
```

### TLS with HTTP

```python
client = connect(
    "https://secure.host/services/jet",
    tls_ca="/path/to/ca.pem",
)
```

### TLS Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `tls_ca` | `str` or `bytes` | PEM-encoded CA certificate file path, or raw PEM bytes |
| `tls_cert` | `str` or `bytes` | PEM-encoded client certificate for mutual-TLS |
| `tls_key` | `str` or `bytes` | PEM-encoded private key for mutual-TLS |

For gRPC, raw `bytes` are passed directly to `grpc.ssl_channel_credentials()`.
For HTTP, file paths are passed to `httpx` via its SSL context.

---

## Combining Auth and TLS

Authentication and TLS are independent — combine them as needed:

```python
from mill import connect
from mill.auth import BasicAuth

client = connect(
    "grpcs://secure.host:443",
    auth=BasicAuth("user", "pass"),
    tls_ca="/path/to/ca.pem",
)
```

---

## Error Handling

Authentication failures raise `MillAuthError`:

```python
from mill import connect, MillAuthError
from mill.auth import BasicAuth

try:
    client = connect("grpc://localhost:9090", auth=BasicAuth("bad", "creds"))
    client.handshake()
except MillAuthError as e:
    print(f"Auth failed: {e}")
    # Auth failed: Authentication failed
```

This maps from gRPC `UNAUTHENTICATED` / `PERMISSION_DENIED` status codes
and HTTP 401 / 403 responses.
