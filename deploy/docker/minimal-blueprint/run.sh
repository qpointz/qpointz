#!/usr/bin/env bash
# Run Mill minimal-blueprint locally via Docker.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DETACH=false

usage() {
  cat <<'EOF'
Usage: ./run.sh [options]

  --data-dir PATH   Host directory mounted read-only at /data (default: ./data under blueprint)
  --image IMAGE     Container image (overrides MILL_IMAGE env / .env)
  --port PORT       Host port mapped to 8080 (default: 8080)
  --detach, -d      Run container in background
  -h, --help        Show this help

Environment (or .env in script directory):
  MILL_IMAGE          Container image (default: qpointz/mill-service-minimal:latest)
  MILL_DATA_DIR       Host path mounted at /data (default: <blueprint>/data)
  MILL_HOST_PORT      Published port (default: 8080)
  MILL_CONTAINER_NAME Docker name (default: mill-minimal-blueprint)

Precedence: CLI flags > shell env > .env > defaults.
Edit config/*.yml to change schema name, cache, or auth.
EOF
}

if [[ -f "$SCRIPT_DIR/.env" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$SCRIPT_DIR/.env"
  set +a
fi

MILL_IMAGE="${MILL_IMAGE:-qpointz/mill-service-minimal:latest}"
MILL_DATA_DIR="${MILL_DATA_DIR:-$SCRIPT_DIR/data}"
MILL_HOST_PORT="${MILL_HOST_PORT:-8080}"
MILL_CONTAINER_NAME="${MILL_CONTAINER_NAME:-mill-minimal-blueprint}"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --data-dir) MILL_DATA_DIR="$2"; shift 2 ;;
    --image) MILL_IMAGE="$2"; shift 2 ;;
    --port) MILL_HOST_PORT="$2"; shift 2 ;;
    --detach | -d) DETACH=true; shift ;;
    -h | --help) usage; exit 0 ;;
    *)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

if ! command -v docker &>/dev/null; then
  echo "docker not found in PATH" >&2
  exit 1
fi

# Relative paths are under the blueprint root (not the caller's cwd).
case "$MILL_DATA_DIR" in
  /* | [a-zA-Z]:*) ;;
  *) MILL_DATA_DIR="$SCRIPT_DIR/$MILL_DATA_DIR" ;;
esac
mkdir -p "$MILL_DATA_DIR"

if command -v cygpath &>/dev/null; then
  DATA_DIR_ABS="$(cygpath -m "$(cd "$MILL_DATA_DIR" && pwd)")"
  CONFIG_DIR_ABS="$(cygpath -m "$SCRIPT_DIR/config")"
else
  DATA_DIR_ABS="$(cd "$MILL_DATA_DIR" && pwd)"
  CONFIG_DIR_ABS="$(cd "$SCRIPT_DIR/config" && pwd)"
fi

if ! find "$MILL_DATA_DIR" -mindepth 2 -type f \( \
    -name '*.parquet' -o -name '*.csv' -o -name '*.avro' -o -name '*.xlsx' \
  \) -print -quit 2>/dev/null | grep -q .; then
  echo "WARNING: no data files under $MILL_DATA_DIR" >&2
  echo "         Add e.g. data/cities/cities.parquet (host) -> /data/cities/cities.parquet (container)" >&2
fi

docker rm -f "$MILL_CONTAINER_NAME" >/dev/null 2>&1 || true

args=(
  run --rm --name "$MILL_CONTAINER_NAME"
  -p "${MILL_HOST_PORT}:8080"
  -v "${DATA_DIR_ABS}:/data:ro"
  -v "${CONFIG_DIR_ABS}/application.yml:/app/config/application.yml:ro"
  -v "${CONFIG_DIR_ABS}/flow.yml:/app/config/flow/flow.yml:ro"
  -v "${CONFIG_DIR_ABS}/auth.yml:/app/config/auth/auth.yml:ro"
)

if [[ "$DETACH" == true ]]; then
  args+=(-d)
else
  args+=(-it)
fi
args+=("$MILL_IMAGE")

echo "Image:      $MILL_IMAGE"
echo "Data:       ${DATA_DIR_ABS} -> /data"
echo "Config:     ${CONFIG_DIR_ABS}"
echo "URL:        http://localhost:${MILL_HOST_PORT}"
echo "Health:     http://localhost:${MILL_HOST_PORT}/actuator/health"
echo ""
docker "${args[@]}"

if [[ "$DETACH" == true ]]; then
  echo "Logs: docker logs -f ${MILL_CONTAINER_NAME}"
fi
