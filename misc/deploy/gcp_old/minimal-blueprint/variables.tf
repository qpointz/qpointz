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
# Naming — prefixes resource names: {deployment_name}-run-service, -bucket, -runtime, etc.
# -----------------------------------------------------------------------------

variable "deployment_name" {
  description = "Short stack prefix (lowercase, hyphens). Used in Cloud Run service id, bucket name, secret ids, and runtime SA account_id segment."
  type        = string
  default     = "mill-minimal-blpr"
}

# -----------------------------------------------------------------------------
# Mill container
# -----------------------------------------------------------------------------

variable "image_version" {
  description = "Container image reference for the Cloud Run revision (e.g. docker.io/qpointz/mill-service-minimal:0.8.0rc3). Pin a digest or tag for reproducible deploys."
  type        = string
  default     = "qpointz/mill-service-minimal:latest"
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
  description = "Maps to mill.data.backend.flow.cache.schema.enabled in the rendered application.yml. When true, Mill reuses resolved Flow/Calcite schemas across requests (fewer GCS listings on repeat queries). Passed to templatefile as the YAML boolean true/false."
  type        = bool
  default     = true
}

variable "schema_cache_ttl" {
  description = "Maps to mill.data.backend.flow.cache.schema.ttl in the rendered application.yml. Spring duration after which cached schemas expire (e.g. 30s, 1m, 5m). Only meaningful when schema_cache_enabled is true; shorten after bucket uploads so new tables appear sooner without disabling cache."
  type        = string
  default     = "5m"
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
# Access
# -----------------------------------------------------------------------------

variable "allow_unauthenticated" {
  description = "If true, grants roles/run.invoker to allUsers (public HTTPS). Set false for IAM-only invoke."
  type        = bool
  default     = true
}

variable "gcs_force_destroy" {
  description = "If true, deleting the Terraform bucket resource removes all objects (dev-friendly). Set false when the bucket holds production data."
  type        = bool
  default     = true
}
