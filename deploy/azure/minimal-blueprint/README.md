# Mill minimal blueprint (Azure Container Apps)

OpenTofu root module that deploys a **minimal Mill service** on **Azure Container Apps** with:

- a dedicated **storage account** (blob container for query data only),
- **Flow** backend reading Parquet, CSV, Avro, and Excel from blob storage (`storage.type: adls`),
- Mill **application**, **flow**, and **auth** YAML rendered from templates into **Azure Key Vault** and mounted at `/app/config` via the runtime **managed identity** (no storage account keys),
- a **user-assigned managed identity** with **Storage Blob Data Reader** on the account (ambient credentials for Flow).

This stack is intentionally small: no PostgreSQL, no OpenAI, no sync scripts. Use it to try Mill on Azure with file-backed data only.

---

## What gets created

For `deployment_name = "mill-minimal-blpr"` (default):

| Resource | Name / pattern |
|----------|----------------|
| Resource group | `{deployment_name}-rg` |
| Log Analytics workspace | `{deployment_name}-law` |
| Container Apps environment | `{deployment_name}-aca-env` |
| Container App | `{deployment_name}-aca` |
| Storage account | explicit `storage_account_name`, or auto: `{deployment}{hash\|random suffix}` |
| Blob container | `data` (Flow objects under `data/` prefix) |
| Key Vault | `{deployment}{hash}-kv` — secrets `application-yml`, `flow-yml`, `auth-yml` |
| User-assigned identity | `{deployment_name}-runtime` |

**Container image:** `var.image_version` (default `qpointz/mill-service-minimal:0.8.0rc3` in [`variables.tf`](variables.tf); there is no `latest` tag on Docker Hub).

**Ingress:** public HTTPS when `allow_unauthenticated = true` (default) — external ingress on the Container App FQDN. Set `allow_unauthenticated = false` for **internal-only** ingress (no Entra ID gate in this blueprint).

---

## Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│  Container App ({deployment_name}-aca)                      │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  mill-service-minimal container                       │  │
│  │  /app/config/application.yml  ← Key Vault (secret volume) │  │
│  │  /app/config/flow/flow.yml    ← Key Vault                 │  │
│  │  /app/config/auth/auth.yml    ← Key Vault                 │  │
│  └───────────────────────────────────────────────────────┘  │
│         │ UAMI + DefaultAzureCredential (AZURE_CLIENT_ID)   │
└─────────┼───────────────────────────────────────────────────┘
          │
          ├──────────────────────────────────────────────────────► Key Vault (config secrets)
          ▼
   Blob container  https://{account}.blob.core.windows.net/data/{table}/…
```

Flow descriptor ([`config/flow.tpl.yml`](config/flow.tpl.yml)) scans the `data/` prefix in the blob container. Each **table** is the **parent folder** of a file; the **reader** is chosen by extension:

```text
…/data/cities/cities.parquet   →  schema {schema_name}, table `cities` (parquet)
…/data/orders/orders.csv       →  schema {schema_name}, table `orders` (csv)
```

Regex pattern (per reader): `(?<table>[^/]+)/[^/]+\.{ext}$` — do **not** prefix with greedy `.*` before the capture group.

Blob auth uses **ambient credentials** (`preferAmbientCredentials: true`) — the runtime managed identity must have **Storage Blob Data Reader** on the storage account (granted by this module). `AZURE_CLIENT_ID` is set to the UAMI client ID so `DefaultAzureCredential` selects the correct identity.

### Config delivery (Key Vault + managed identity)

Rendered templates are stored as **Key Vault secrets** (`application-yml`, `flow-yml`, `auth-yml`). The Container App references them with **Key Vault secret IDs** and the **runtime user-assigned managed identity** — the same pattern as GCP **Secret Manager** + service account. **No storage account key** is used for config.

Key Vault secret names cannot contain `.` (`application-yml`, etc.). An **azapi** patch maps them to standard paths under `/app/config/` (`application.yml`, `flow/flow.yml`, `auth/auth.yml`) so Spring Boot discovers config the same way as the Docker and GCP blueprints — no `SPRING_CONFIG_LOCATION` override.

### Java Development Stack (optional, default on)

This blueprint enables the **Java Development Stack** for Spring Boot:

| Feature | Terraform | Microsoft docs |
|---------|-----------|----------------|
| Development stack `runtime=java` | `enable_java_development_stack` | [Java metrics](https://learn.microsoft.com/en-us/azure/container-apps/java-metrics) |
| JVM core metrics | `java_enable_metrics` | Portal **Metrics** → Java category |
| Platform Java agent | `java_enable_java_agent` | Injects client config for Admin for Spring (no extra Maven dep) |
| **Admin for Spring** component + bind | `enable_admin_for_spring` | [Admin for Spring](https://learn.microsoft.com/en-us/azure/container-apps/java-admin?tabs=azure-cli) |

`azurerm_container_app` cannot set `configuration.runtime.java` or `template.serviceBinds`; [`java.tf`](java.tf) uses the **azapi** provider to apply them after each apply (so they are not reset to Generic on redeploy).

After apply:

```bash
tofu output -raw java_admin_dashboard_url
```

Actuator endpoints are exposed in `application.tpl.yml` when Admin for Spring is enabled (`management.endpoints.web.exposure.include=*`). Your account needs **`Microsoft.App/managedEnvironments/write`** on the ACA environment to open the dashboard in the portal.

Disable the stack (metrics/agent/admin only) in tfvars:

```hcl
enable_java_development_stack = false
enable_admin_for_spring       = false
```

---

## Prerequisites

- [OpenTofu](https://opentofu.org/) or Terraform **≥ 1.6**
- An Azure **subscription** with permission to create resource groups, Container Apps, storage, and role assignments
- [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) for blob upload and troubleshooting
- **`Microsoft.App` registered** on the subscription (Container Apps). The `azurerm` provider auto-registers `Microsoft.OperationalInsights` and `Microsoft.ManagedIdentity` when needed; **`Microsoft.App` is not** in that set — register it once before the first apply:

  ```bash
  az login
  az account set --subscription "<subscription-id>"
  az provider register --namespace Microsoft.App --wait
  az provider show --namespace Microsoft.App --query registrationState -o tsv
  # must print: Registered
  ```

---

## Quick start

```bash
cd deploy/azure/minimal-blueprint

cp terraform.tfvars.example terraform.tfvars
# Edit subscription_id, location, and optionally image_version / schema_name

tofu init
tofu plan -var-file=terraform.tfvars
tofu apply -var-file=terraform.tfvars
```

`terraform.tfvars` is gitignored. Do not commit it if it contains sensitive IDs.

See [`terraform.tfvars.example`](terraform.tfvars.example) for all inputs and commented defaults.

### 1. Upload sample data

```bash
ACCOUNT=$(tofu output -raw storage_account_name)
CONTAINER=$(tofu output -raw blob_container_name)
PREFIX=$(tofu output -raw blob_data_prefix)

# Parquet example → table "cities"
az storage blob upload \
  --account-name "$ACCOUNT" \
  --container-name "$CONTAINER" \
  --name "${PREFIX}cities/cities.parquet" \
  --file ./local/cities.parquet \
  --auth-mode login

# CSV example (semicolon delimiter matches flow.tpl.yml) → table "orders"
az storage blob upload \
  --account-name "$ACCOUNT" \
  --container-name "$CONTAINER" \
  --name "${PREFIX}orders/orders.csv" \
  --file ./local/orders.csv \
  --auth-mode login
```

PowerShell:

```powershell
$account = tofu output -raw storage_account_name
$container = tofu output -raw blob_container_name
$prefix = tofu output -raw blob_data_prefix

az storage blob upload `
  --account-name $account `
  --container-name $container `
  --name "${prefix}cities/cities.parquet" `
  --file .\local\cities.parquet `
  --auth-mode login
```

### 2. Health check

```bash
curl -sS "$(tofu output -raw health_check_url)"
```

Service URL:

```bash
tofu output -raw container_app_url
```

Flow schema name:

```bash
tofu output -raw flow_schema_name
```

Default **`minimal`** unless you set `schema_name` in tfvars.

---

## Configuration templates

Terraform renders these on each apply and writes them to **Key Vault**:

| Template | Key Vault secret | Mount path | Purpose |
|----------|------------------|------------|---------|
| [`config/application.tpl.yml`](config/application.tpl.yml) | `application-yml` | `/app/config/application.yml` | Mill app: Flow backend, UI off, gRPC off, configurable Flow **schema** cache |
| [`config/flow.tpl.yml`](config/flow.tpl.yml) | `flow-yml` | `/app/config/flow/flow.yml` | ADLS/blob storage + multi-format readers |
| [`config/auth.tpl.yml`](config/auth.tpl.yml) | `auth-yml` | `/app/config/auth/auth.yml` | Basic-auth user store (inactive until security is enabled) |

### Storage account name

Three ways to set the globally unique storage account name:

| Approach | tfvars | Result |
|----------|--------|--------|
| **Explicit** | `storage_account_name = "myuniqueacct01"` | Uses your name as-is (validated: 3–24 `a-z0-9`) |
| **Hash** (default) | omit `storage_account_name`; `storage_account_name_generation = "hash"` | `{deployment_base}{6-char sha256}` — stable per subscription + region + deployment |
| **Random** | omit `storage_account_name`; `storage_account_name_generation = "random"` | `{deployment_base}{6-char random_id}` — stable in Terraform state after first apply |

`flow.tpl.yml` always receives the resolved name via `tofu output -raw storage_account_name`.

After the first successful apply, set **`storage_account_name`** in tfvars to the output value so later applies do not replace the account when `deployment_name` or hash inputs change.

### Customizing config

1. Edit the relevant `config/*.tpl.yml`.
2. Run `tofu apply -var-file=terraform.tfvars`.
3. Container Apps picks up a **new revision** when Key Vault secret values change; if the running revision does not reload, trigger a new revision (e.g. `az containerapp revision copy` or a no-op template change).

Template variables (passed from [`locals.tf`](locals.tf) / [`main.tf`](main.tf) via `templatefile`):

**`config/flow.tpl.yml`**

| Template arg | Source | Meaning |
|--------------|--------|---------|
| `schema_name` | `var.schema_name` | Flow / Calcite schema name |
| `storage_account_name` | computed account name | Blob endpoint account segment |
| `container_name` | `data` | Blob container for Flow data |

**`config/application.tpl.yml`**

| Template arg | Source | YAML path | Meaning |
|--------------|--------|-----------|---------|
| `schema_cache_enabled` | `var.schema_cache_enabled` | `mill.data.backend.flow.cache.schema.enabled` | Reuse resolved Flow schemas |
| `schema_cache_ttl` | `var.schema_cache_ttl` | `mill.data.backend.flow.cache.schema.ttl` | Schema cache TTL (e.g. `5m`) |

### Enabling HTTP Basic auth

Same as GCP: set `mill.security.enable: true` and `basic.file-store: file:/app/config/auth/auth.yml` in `application.tpl.yml`, then replace demo users in `auth.tpl.yml`.

---

## Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `subscription_id` | yes | — | Azure subscription ID |
| `location` | yes | — | Azure region |
| `deployment_name` | no | `mill-minimal-blpr` | Prefix for resource names |
| `storage_account_name` | no | `null` (auto) | Explicit storage account name (3–24 `a-z0-9`) |
| `storage_account_name_generation` | no | `hash` | When name is auto: `hash` or `random` |
| `image_version` | no | `qpointz/mill-service-minimal:0.8.0rc3` | Container image (pin a published tag) |
| `schema_name` | no | `minimal` | Flow schema name in `flow.tpl.yml` |
| `schema_cache_enabled` | no | `true` | Flow schema cache on/off |
| `schema_cache_ttl` | no | `"5m"` | Schema cache TTL |
| `min_replicas` | no | `0` | Min replicas (0 = scale to zero) |
| `max_replicas` | no | `3` | Max replicas |
| `container_cpu` | no | `0.5` | CPU cores |
| `container_memory` | no | `"1Gi"` | Memory limit |
| `allow_unauthenticated` | no | `true` | External HTTPS ingress |
| `storage_allow_nested_items_to_be_deleted` | no | `true` | Reserved; see destroy notes below |
| `key_vault_purge_protection_enabled` | no | `false` | Key Vault purge protection (production: `true`) |
| `enable_java_development_stack` | no | `true` | Java runtime + azapi patch on Container App |
| `java_enable_metrics` | no | `true` | JVM metrics in Azure Monitor |
| `java_enable_java_agent` | no | `false` | Platform Java agent (can break slim images; enable for Admin client injection) |
| `enable_admin_for_spring` | no | `true` | Managed Admin for Spring component + bind |
| `java_admin_component_name` | no | `admin` | Admin component name in the ACA environment |

Full descriptions: [`variables.tf`](variables.tf). Outputs: [`outputs.tf`](outputs.tf).

---

## Differences from GCP minimal blueprint

| Topic | GCP | Azure (this module) |
|-------|-----|---------------------|
| Compute | Cloud Run | Container Apps |
| Data store | GCS bucket | Storage account blob container |
| Config | Secret Manager volume mounts | Key Vault secrets + UAMI + Secret volume |
| Flow `storage.type` | `gcs` | `adls` (blob endpoint + managed identity) |
| Public access | `allUsers` run.invoker | External ingress FQDN |
| Restricted access | IAM-only invoke | Internal ingress only (`allow_unauthenticated = false`) |
| Bucket empty on destroy | `gcs_force_destroy` | Empty blob container before `tofu destroy` if Terraform reports dependency errors |

---

## Security notes (demo defaults)

| Setting | Default | Action for production |
|---------|---------|------------------------|
| Ingress | `allow_unauthenticated = true` | Set `false` or add Entra ID / App Gateway |
| Basic auth password | `{noop}admin` in `auth.tpl.yml` | Change users/passwords |
| Storage | Dev account in one RG | Separate subscription/RG; disable nested delete assumptions |
| Image | `mill-service-minimal:0.8.0rc3` | Pin `image_version` to a published tag or digest |
| Config access | Key Vault + **Key Vault Secrets User** on runtime UAMI | No storage keys; deploy principal needs **Key Vault Secrets Officer** to apply |
| OpenTofu state | — | Key Vault secret **values** are in state — treat state as sensitive; use remote encrypted backend in production |

---

## Destroy

```bash
tofu destroy -var-file=terraform.tfvars
```

If destroy fails because the storage account is not empty, delete blobs under `data/`, then retry. With `key_vault_purge_protection_enabled = true`, allow soft-delete retention before purge. The variable `storage_allow_nested_items_to_be_deleted` documents intent for dev stacks; the current `azurerm` provider version does not map it to a storage account argument.

---

## Troubleshooting

**No tables / empty schema**

- Confirm blobs exist under `{container}/data/{table}/file.{parquet|csv|avro|xlsx}`.
- Verify the runtime identity has **Storage Blob Data Reader** on the storage account.
- Check regex in `flow.tpl.yml` matches your paths.

**New blobs uploaded but tables still missing**

- With `schema_cache_enabled = true`, wait for `schema_cache_ttl` or shorten TTL and re-apply.

**Config change not picked up**

- Re-run `tofu apply` to update Key Vault secrets; ensure the Container App revision reloads (new revision after secret update).

**Admin for Spring delete fails with 409**

- The Container App must be **unbound** before the `admin` Java component can be deleted.
- Set `enable_admin_for_spring = false` and apply (clears `serviceBinds` first), or run:
  `az containerapp update --name <app> -g <rg> --unbind admin`
- Then re-run `tofu apply`.

**Key Vault / config mount errors**

- Runtime UAMI needs **Key Vault Secrets User** on the vault (granted by this module).
- ACA secret `identity` must be the same UAMI as `AZURE_CLIENT_ID`.
- Confirm secrets exist: `az keyvault secret list --vault-name "$(tofu output -raw key_vault_name)"`.

**Managed identity / 403 on blob read**

- Confirm `AZURE_CLIENT_ID` matches `tofu output -raw runtime_identity_client_id`.
- Role assignment can take a few minutes after first apply.

**Wrong table names**

- Ensure flow regex does not use a leading greedy `.*` before `(?<table>…)`.

**Container App does not start**

- Check revision logs: `az containerapp logs show -n <app> -g <rg> --follow`.
- Public images must be pullable from the environment (Docker Hub rate limits).

**`MANIFEST_UNKNOWN` / `unknown tag=latest`**

- `qpointz/mill-service-minimal` has **no `latest` tag** on Docker Hub. Use a published tag, e.g. `image_version = "qpointz/mill-service-minimal:0.8.0rc3"` in tfvars, then `tofu apply`.
- List tags: `curl -s https://hub.docker.com/v2/repositories/qpointz/mill-service-minimal/tags?page_size=5`

**`MissingSubscriptionRegistration` / `Microsoft.App`**

- Register once: `az provider register --namespace Microsoft.App --wait`
- Check state: `az provider show --namespace Microsoft.App --query registrationState -o tsv` → `Registered`
- Re-run `tofu apply`. If a prior apply failed mid-way, storage and Key Vault may already exist — apply continues from the ACA step.

---

## Smoke test checklist

After `tofu apply`:

1. `tofu output -raw health_check_url` → `curl` returns `{"status":"UP"}` (or similar).
2. Upload at least one Parquet or CSV under `data/{table}/`.
3. Query via Mill HTTP API or JDBC using `tofu output -raw container_app_url`.

---

## Related

- [`../../gcp/minimal-blueprint/README.md`](../../gcp/minimal-blueprint/README.md) — GCP Cloud Run variant
- [`../../docker/minimal-blueprint/README.md`](../../docker/minimal-blueprint/README.md) — local Docker variant
- [`../../README.md`](../../README.md) — deploy overview
- [`docs/public/src/sources/storages/azure.md`](../../../docs/public/src/sources/storages/azure.md) — Flow `adls` storage reference
