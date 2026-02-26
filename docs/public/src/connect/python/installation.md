# Installation

Detailed installation guide for mill-py covering all supported environments
and configurations.

---

## Requirements

- **Python**: 3.10, 3.11, 3.12, or 3.13
- **OS**: Linux, macOS, Windows (any platform supported by grpcio)

---

## Install from PyPI

### Core (gRPC + HTTP)

```bash
pip install qpointz-mill-py
```

This installs the core client with gRPC and HTTP transport support. You can
query data and iterate results as Python dicts — no additional dependencies
needed.

### With DataFrame Extras

```bash
pip install qpointz-mill-py[arrow]       # + PyArrow
pip install qpointz-mill-py[pandas]      # + pandas (includes Arrow)
pip install qpointz-mill-py[polars]      # + polars (includes Arrow)
pip install qpointz-mill-py[all]         # all extras
```

| Extra | Adds | Use Case |
|-------|------|----------|
| `arrow` | `pyarrow` | Columnar data, `result.to_arrow()` |
| `pandas` | `pyarrow`, `pandas` | DataFrames, `result.to_pandas()` |
| `polars` | `pyarrow`, `polars` | Fast DataFrames, `result.to_polars()` |
| `all` | All of the above | Everything |

---

## Virtual Environment (Recommended)

Isolate mill-py from your system Python:

```bash
# Create and activate
python -m venv .venv
source .venv/bin/activate        # Linux / macOS
# .venv\Scripts\activate         # Windows

# Install
pip install qpointz-mill-py[all]

# Verify
python -c "from mill import connect; print('OK')"
```

---

## Install from Source (Development)

For contributing or working with the latest code:

```bash
# Clone the repository
git clone https://github.com/qpointz/qpointz.git
cd qpointz

# Install in editable mode with all extras
pip install -e "clients/mill-py[all]"

# Or using the Makefile
cd clients && make install
```

Editable mode (`-e`) means code changes take effect immediately without
reinstalling.

### Development Dependencies

For running tests and building:

```bash
cd clients/mill-py
pip install poetry
poetry install --all-extras
```

This installs dev dependencies (`pytest`, `pytest-asyncio`, `pytest-mock`,
`pytest-cov`, `grpcio-tools`) alongside the runtime dependencies.

---

## Verify Installation

```python
# Basic import check
from mill import connect, MillClient, MillType
print("Core OK")

# Check extras
try:
    from mill.extras.arrow import result_to_arrow
    print("Arrow OK")
except ImportError:
    print("Arrow not installed")

try:
    from mill.extras.pandas import result_to_pandas
    print("pandas OK")
except ImportError:
    print("pandas not installed")

try:
    from mill.extras.polars import result_to_polars
    print("polars OK")
except ImportError:
    print("polars not installed")

# Check async
from mill.aio import connect as aconnect
print("Async OK")
```

---

## Dependencies

### Core (always installed)

| Package | Purpose |
|---------|---------|
| `grpcio` | gRPC transport (HTTP/2, streaming) |
| `protobuf` | Protocol Buffers serialization |
| `httpx` | HTTP transport (JSON and protobuf encoding) |

### Optional (via extras)

| Package | Extra |
|---------|-------|
| `pyarrow` | `arrow`, `pandas`, `polars`, `all` |
| `pandas` | `pandas`, `all` |
| `polars` | `polars`, `all` |

---

## Uninstall

```bash
pip uninstall qpointz-mill-py

# Or using the Makefile
cd clients && make uninstall
```

---

## Troubleshooting

### `grpcio` build failures

On some platforms, `grpcio` needs to be compiled from source. If you see
build errors:

```bash
# Install system dependencies (Ubuntu/Debian)
sudo apt-get install python3-dev build-essential

# Or use a pre-built wheel
pip install --only-binary=grpcio qpointz-mill-py
```

### Import errors after install

If `import mill` fails, verify you're using the correct Python environment:

```bash
which python
python -c "import sys; print(sys.executable)"
pip show qpointz-mill-py
```

### Version conflicts

If you have dependency conflicts, use a clean virtual environment:

```bash
python -m venv fresh-env
source fresh-env/bin/activate
pip install qpointz-mill-py[all]
```
