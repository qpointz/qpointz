# -----------------------------------------------------------------------------
# Input variables — see terraform.tfvars.example and README.md.
# -----------------------------------------------------------------------------

# -----------------------------------------------------------------------------
# Azure target
# -----------------------------------------------------------------------------

variable "subscription_id" {
  description = "Azure subscription ID where the resource group, Container Apps, storage, and managed identity are created."
  type        = string
}

variable "location" {
  description = "Azure region for the resource group, Container Apps environment, and storage account."
  type        = string
}

# -----------------------------------------------------------------------------
# Naming — prefixes resource names: {deployment_name}-rg, -aca, -sa, etc.
# -----------------------------------------------------------------------------

variable "deployment_name" {
  description = "Short stack prefix (lowercase, hyphens). Used in resource group, Container App, storage account suffix, and Key Vault name."
  type        = string
  default     = "mill-minimal-blpr"
}

# -----------------------------------------------------------------------------
# Storage account naming
# -----------------------------------------------------------------------------

variable "storage_account_name" {
  description = "Optional explicit storage account name (3–24 lowercase alphanumeric, globally unique). When null, the name is built from deployment_name plus a suffix from storage_account_name_generation."
  type        = string
  default     = null
  nullable    = true

  validation {
    condition     = var.storage_account_name == null || can(regex("^[a-z0-9]{3,24}$", var.storage_account_name))
    error_message = "When set, storage_account_name must be 3–24 lowercase letters or digits only."
  }
}

variable "storage_account_name_generation" {
  description = "When storage_account_name is null: hash (deterministic suffix from subscription, location, deployment) or random (random_id suffix, stable in state after first apply). Ignored when storage_account_name is set."
  type        = string
  default     = "hash"

  validation {
    condition     = contains(["hash", "random"], var.storage_account_name_generation)
    error_message = "storage_account_name_generation must be \"hash\" or \"random\"."
  }
}

# -----------------------------------------------------------------------------
# Mill container
# -----------------------------------------------------------------------------

variable "image_version" {
  description = "Container image reference for the Container App revision (e.g. qpointz/mill-service-minimal:0.8.0rc3). Pin a tag or digest for reproducible deploys."
  type        = string
  default     = "qpointz/mill-service-minimal:0.8.0rc3"
}

variable "schema_name" {
  description = "Flow / Calcite schema name rendered into config/flow.tpl.yml (top-level name field). Exposed as output flow_schema_name."
  type        = string
  default     = "minimal"
}

# -----------------------------------------------------------------------------
# Flow backend — schema cache (application.tpl.yml → mill.data.backend.flow.cache.schema)
# -----------------------------------------------------------------------------

variable "schema_cache_enabled" {
  description = "Maps to mill.data.backend.flow.cache.schema.enabled in the rendered application.yml. When true, Mill reuses resolved Flow/Calcite schemas across requests (fewer blob listings on repeat queries). Passed to templatefile as the YAML boolean true/false."
  type        = bool
  default     = true
}

variable "schema_cache_ttl" {
  description = "Maps to mill.data.backend.flow.cache.schema.ttl in the rendered application.yml. Spring duration after which cached schemas expire (e.g. 30s, 1m, 5m). Only meaningful when schema_cache_enabled is true; shorten after blob uploads so new tables appear sooner without disabling cache."
  type        = string
  default     = "5m"
}

# -----------------------------------------------------------------------------
# Container Apps scaling and resources
# -----------------------------------------------------------------------------

variable "min_replicas" {
  description = "Minimum replicas for the Container App (0 allows scale-to-zero when idle)."
  type        = number
  default     = 0
}

variable "max_replicas" {
  description = "Maximum replicas for the Container App."
  type        = number
  default     = 3
}

variable "container_cpu" {
  description = "CPU cores for the container (Container Apps format, e.g. 0.25, 0.5, 1)."
  type        = number
  default     = 0.5
}

variable "container_memory" {
  description = "Memory limit for the container (e.g. \"0.5Gi\", \"1Gi\", \"2Gi\")."
  type        = string
  default     = "1Gi"
}

# -----------------------------------------------------------------------------
# Access and storage lifecycle
# -----------------------------------------------------------------------------

variable "allow_unauthenticated" {
  description = "If true, enables external HTTPS ingress on the Container App (public FQDN). If false, ingress is internal-only (VNet integration not included in this blueprint)."
  type        = bool
  default     = true
}

variable "storage_allow_nested_items_to_be_deleted" {
  description = "Documents dev-friendly destroy intent (empty storage before tofu destroy). Not wired to a provider argument in azurerm 4.x; see README destroy section."
  type        = bool
  default     = true
}

variable "key_vault_purge_protection_enabled" {
  description = "Enable purge protection on the config Key Vault. Set true for production; false allows faster destroy in dev stacks."
  type        = bool
  default     = false
}

# -----------------------------------------------------------------------------
# Java Development Stack (Admin for Spring, JVM metrics, Java agent)
# -----------------------------------------------------------------------------

variable "enable_java_development_stack" {
  description = "Apply Java Development Stack settings on the Container App via azapi (runtime=java). Re-applied after each azurerm_container_app update."
  type        = bool
  default     = true
}

variable "java_enable_metrics" {
  description = "Enable JVM core metrics (configuration.runtime.java.enableMetrics). Requires enable_java_development_stack."
  type        = bool
  default     = true
}

variable "java_enable_java_agent" {
  description = "Inject the platform Java agent (configuration.runtime.java.javaAgent.enabled). Disabled by default — the agent can fail on slim JRE images (jdk.management.agent). Enable only when you need Admin-for-Spring client injection without a Maven dependency."
  type        = bool
  default     = false
}

variable "enable_admin_for_spring" {
  description = "Create a managed Admin for Spring Java component and bind the Mill Container App to it."
  type        = bool
  default     = true
}

variable "java_admin_component_name" {
  description = "Name of the Admin for Spring Java component in the Container Apps environment."
  type        = string
  default     = "admin"
}

variable "java_admin_min_replicas" {
  description = "Minimum replicas for the Admin for Spring component (fixed 0.5 CPU / 1 GiB per Microsoft docs)."
  type        = number
  default     = 1
}

variable "java_admin_max_replicas" {
  description = "Maximum replicas for the Admin for Spring component."
  type        = number
  default     = 1
}
