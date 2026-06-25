# -----------------------------------------------------------------------------
# Shared names, tags, and rendered config (templatefile → YAML strings).
# -----------------------------------------------------------------------------

locals {
  tags = {
    mill-managed-by   = "azure-container-apps"
    mill-deploy-stack = "minimal-blueprint"
  }

  deployment = var.deployment_name

  # Storage account: 3–24 lowercase alphanumeric, globally unique.
  # Explicit var.storage_account_name wins; otherwise base + hash or random suffix.
  storage_account_base = substr(replace(lower(local.deployment), "-", ""), 0, 12)

  storage_account_hash_suffix = substr(
    sha256("${var.subscription_id}:${var.location}:${local.deployment}"),
    0,
    6,
  )

  storage_account_random_suffix = length(random_id.storage_suffix) > 0 ? random_id.storage_suffix[0].hex : ""

  storage_account_auto_suffix = var.storage_account_name_generation == "random" ? local.storage_account_random_suffix : local.storage_account_hash_suffix

  storage_account_auto_name = substr("${local.storage_account_base}${local.storage_account_auto_suffix}", 0, 24)

  storage_account_name = coalesce(var.storage_account_name, local.storage_account_auto_name)

  blob_container_name = "data"
  blob_data_prefix    = "data/"

  # Key Vault name: 3–24 alphanumeric, globally unique (no hyphens).
  key_vault_hash_suffix = substr(
    sha256("${var.subscription_id}:${var.location}:${local.deployment}:kv"),
    0,
    6,
  )

  key_vault_name = substr("${local.storage_account_base}kv${local.key_vault_hash_suffix}", 0, 24)

  ########################################################################
  #### Java Development Stack — actuator exposure for Admin for Spring
  ########################################################################

  expose_actuator_endpoints = var.enable_java_development_stack && var.enable_admin_for_spring

  ########################################################################
  #### Rendered Mill config (stored in Key Vault on apply)
  ########################################################################

  application_yml_content = templatefile("${path.module}/config/application.tpl.yml", {
    schema_cache_enabled      = var.schema_cache_enabled ? "true" : "false"
    schema_cache_ttl          = var.schema_cache_ttl
    expose_actuator_endpoints = local.expose_actuator_endpoints
  })

  flow_yml_content = templatefile("${path.module}/config/flow.tpl.yml", {
    schema_name          = var.schema_name
    storage_account_name = local.storage_account_name
    container_name       = local.blob_container_name
  })

  auth_yml_content = templatefile("${path.module}/config/auth.tpl.yml", {})
}
