# Load KEY=VALUE pairs from a file into the environment (no shell execution).
# Safe for values with spaces (unquoted). Use double quotes if the value contains #.
#
# Usage: source this file, then: load_dotenv "/path/to/.env"

load_dotenv() {
  local env_file="$1"
  local line key value

  [[ -n "${env_file}" ]] || return 1
  [[ -f "${env_file}" ]] || return 1

  while IFS= read -r line || [[ -n "${line}" ]]; do
    line="${line%$'\r'}"

    [[ "${line}" =~ ^[[:space:]]*# ]] && continue
    [[ -z "${line//[[:space:]]/}" ]] && continue

    if [[ "${line}" =~ ^[[:space:]]*export[[:space:]]+ ]]; then
      line="${line#*export}"
      line="${line#"${line%%[![:space:]]*}"}"
    fi

    [[ "${line}" == *=* ]] || continue

    key="${line%%=*}"
    value="${line#*=}"
    key="${key%"${key##*[![:space:]]}"}"
    key="${key#"${key%%[![:space:]]*}"}"
    value="${value#"${value%%[![:space:]]*}"}"
    value="${value%"${value##*[![:space:]]}"}"

    [[ "${key}" =~ ^[a-zA-Z_][a-zA-Z0-9_]*$ ]] || continue

    if [[ "${value}" == \"*\" ]]; then
      value="${value#\"}"
      value="${value%\"}"
    elif [[ "${value}" == \'*\' ]]; then
      value="${value#\'}"
      value="${value%\'}"
    else
      # Drop inline comment: whitespace then # (keeps values like url#fragment)
      value="$(printf '%s' "${value}" | sed 's/[[:space:]]\{1,\}#.*$//')"
      value="${value%"${value##*[![:space:]]}"}"
    fi

    export "${key}"="${value}"
  done <"${env_file}"
}
