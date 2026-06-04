# -----------------------------------------------------------------------------
# Naming — prefixes resource names: {deployment_name}-run-service, -bucket, -runtime, etc.
# -----------------------------------------------------------------------------

variable "deployment_name" {
  description = "Short stack prefix (lowercase, hyphens). Used in Cloud Run service id, bucket name, secret ids, and runtime SA account_id segment."
  type        = string
  default     = "bp-micro-dep"
}

variable "deployment_stack_name" {
  description = "Short stack prefix (lowercase, hyphens). Used to identify stack deployed"
  type        = string
  default = "bp-micro"
}

variable "mill_version" {
  description = "Mill version to deploy"
  type        = string
}

# -----------------------------------------------------------------------------
# GCP target
# -----------------------------------------------------------------------------

variable "project_id" {
  description = "GCP project ID where APIs, Cloud Run, the bucket, secrets, and the runtime service account are created."
  type        = string
}

variable "region" {
  description = "Region for the Cloud Run service and the GCS bucket (bucket location matches this value)."
  type        = string
}

# -----------------------------------------------------------------------------
# Flow backend — schema cache (application.tpl.yml → mill.data.backend.flow.cache.schema)
# -----------------------------------------------------------------------------

variable "backend" {
  description = "Backend to be used with service available value flow, jdbc"
  default     = "flow"
}

variable "backend_flow_config_as_secret" {
  description = "When true created flow source config as secret"
  type        = bool
  default     = false
}

variable "schema_cache_enabled" {
  description = "Maps to mill.data.backend.flow.cache.schema.enabled in the rendered application.yml. When true, Mill reuses resolved Flow/Calcite schemas across requests (fewer GCS listings on repeat queries). Passed to templatefile as the YAML boolean true/false."
  type        = bool
  default     = true
}

variable "schema_cache_ttl" {
  description = "Maps to mill.data.backend.flow.cache.schema.ttl in the rendered application.yml. Spring duration after which cached schemas expire (e.g. 30s, 1m, 5m). Only meaningful when schema_cache_enabled is true; shorten after bucket uploads so new tables appear sooner without disabling cache."
  type        = string
  default     = "5m"
}

variable "schema_name" {
  description = "Flow / Calcite schema name rendered into config/flow.tpl.yml (top-level name field). Exposed as output flow_schema_name."
  type        = string
  default     = "minimal"
}

# -----------------------------------------------------------------------------
# Cloud Run scaling and resources
# -----------------------------------------------------------------------------

variable "service_max_instance_request_concurrency" {
  description = "Maximum concurrent requests per instance (template.max_instance_request_concurrency)."
  type        = number
  default     = 100
}

variable "service_min_instance_count" {
  description = "Minimum instances (0 allows scale-to-zero when idle)."
  type        = number
  default     = 0
}

variable "service_max_instance_count" {
  description = "Maximum instances for the service revision."
  type        = number
  default     = 3
}

variable "service_limits_cpu" {
  description = "CPU limit for the container (Cloud Run limit string, e.g. \"1\", \"0.5\", \"2\")."
  type        = string
  default     = "1"
}

variable "service_limits_memory" {
  description = "Memory limit for the container (e.g. \"512Mi\", \"1Gi\", \"2Gi\")."
  type        = string
  default     = "2Gi"
}

# -----------------------------------------------------------------------------
# Authentication settings
# -----------------------------------------------------------------------------
variable "auth_enable" {
  description = "Enables authentication"
  type        = bool
  default     = true
}

variable "auth_basic_enable" {
  description = "Enables basic authentication"
  type        = bool
  default     = false
}

variable "auth_basic_store" {
  description = "User store (jpa - db store, file - file store)"
  type = string
}

variable "auth_basic_seed_users" {
  description = "Users for basic authentication"
  type        = list(object({
    user      = string
    password  = optional(string,"")
    groups    = optional(set(string),[])
  }))
  default     = [
    {
      user     = "admin",
      password = "{noop}admin"
      groups   = ["admin"]
    }
  ]
}

# -----------------------------------------------------------------------------
# Db settings
# -----------------------------------------------------------------------------
variable "db" {
  description = "Db to use. values `in-memory`, `cloud-sql`"
  type        = string
  default     = "in-memory"
}

variable "metadata_seeds" {
  description = "metadata seeds"
  type = list(string)
  default = []
}

variable "flow_sample" {
  description = "upload samples to data"
  type = map(list(string))
  default = null
}

# -----------------------------------------------------------------------------
# AI settings
# -----------------------------------------------------------------------------
variable "ai_enable" {
  description = ""
  type = bool
}

variable "ai_openai_key" {
  sensitive = true
  type = string
}


variable "ai_openai_model" {
  type = string
}

variable "ai_openai_embed_model" {
  type = string
}

variable "ai_openai_embed_dim" {
  type = number
}