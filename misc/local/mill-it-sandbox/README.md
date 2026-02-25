# Mill Integration Sandbox (Prototype)

Local two-container setup:

- `mill-it`: Mill service container (same launch style as CI)
- `toolbox`: Ubuntu + Python shell for curl and integration tests

## 1) Optional env overrides

Create `.env` in this folder if you want custom values:

```bash
MILL_SERVICE_IMAGE=registry.qpointz.io/qpointz/qpointz/mill-service-samples:fix-integration
SPRING_PROFILES_ACTIVE=skymill
HOST_HTTP_PORT=8080
HOST_GRPC_PORT=9090
MILL_IT_PROTOCOL=http-json
MILL_IT_PORT=8080
MILL_IT_BASE_PATH=/services/jet
```

## 2) Start services

```bash
./up.sh
```

## 3) Open toolbox shell

```bash
./shell.sh
```

Inside `toolbox`:

```bash
# quick endpoint check
curl -i -X POST "http://mill-it:${MILL_IT_PORT}${MILL_IT_BASE_PATH}/Handshake" \
  -H "Content-Type: application/json" \
  -d '{}'

# run mill-py integration tests
cd /workspace/clients/mill-py
python3 -m venv .venv
source .venv/bin/activate
pip install poetry
poetry install --all-extras
pytest -s -m integration tests/integration
```

## 4) Stop

```bash
./down.sh
```
