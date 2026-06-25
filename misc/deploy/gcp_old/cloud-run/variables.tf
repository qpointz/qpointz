# --- Required core ---
variable "gcp_project_id" {
  description = "GCP project ID (must exist unless you provision it elsewhere)."
  type        = string
}

variable "gcp_region" {
  description = "Cloud Run region."
  type        = string
}

variable "deploy_stack_id" {
  description = "Label mill-deploy-stack value; defaults to cloud_run_service_name when empty."
  type        = string
  default     = ""
}

variable "cloud_run_service_name" {
  description = "Cloud Run service name."
  type        = string
}

variable "mill_docker_image" {
  description = "Full container image reference (e.g. docker.io/qpointz/mill-service-complete:latest)."
  type        = string
}

variable "spring_profiles_active" {
  description = "SPRING_PROFILES_ACTIVE env value."
  type        = string
  default     = "skymill,postgres,secure"
}

variable "mill_db_url" {
  description = "JDBC URL for PostgreSQL (non-secret)."
  type        = string
}

variable "mill_db_username" {
  description = "Database username (non-secret)."
  type        = string
}

variable "mill_db_password" {
  description = "Database password (Secret Manager secret version). Prefer TF_VAR_mill_db_password."
  type        = string
  sensitive   = true
}

variable "openai_api_key" {
  description = "Optional OpenAI API key; empty skips secret and OPENAI_API_KEY env."
  type        = string
  default     = ""
  sensitive   = true
}

variable "mill_cloud_gcp_gcs_project_id" {
  description = "mill.cloud.gcp.gcs.project-id; empty uses gcp_project_id when bucket is set."
  type        = string
  default     = ""
}

variable "gcs_bucket_name" {
  description = "Optional GCS bucket for Skymill config (created when non-empty)."
  type        = string
  default     = ""
}

variable "gcs_bucket_location" {
  description = "Bucket location when gcs_bucket_name is set."
  type        = string
  default     = ""
}

variable "cloud_run_managed_runtime_sa" {
  description = "auto | true | false — managed mill*run SA for GCS ADC."
  type        = string
  default     = "auto"
}

variable "cloud_run_service_account_email" {
  description = "Optional existing runtime SA email; skips managed SA creation."
  type        = string
  default     = ""
}

variable "cloud_sql_connection_name" {
  description = "Optional Cloud SQL connection name project:region:instance."
  type        = string
  default     = ""
}

variable "mill_runtime_config_mount_path" {
  description = "Secret volume mount for application.yml (must not be /config)."
  type        = string
  default     = "/mill-config"
}

variable "custom_application_yml_content" {
  description = "Optional YAML for Secret mount. Ignored when custom_application_yml_file is set."
  type        = string
  default     = ""
  sensitive   = true
}

variable "custom_application_yml_file" {
  description = "Path read at plan time (relative to tofu cwd). Overrides custom_application_yml_content."
  type        = string
  default     = ""
}

variable "cloud_run_cpu" {
  description = "CPU limit string accepted by Cloud Run (e.g. 1, 2)."
  type        = string
  default     = "1"
}

variable "cloud_run_memory" {
  description = "Memory limit (e.g. 512Mi, 2Gi)."
  type        = string
  default     = "2Gi"
}

variable "cloud_run_min_instances" {
  description = "Minimum instances (0 allows scale-to-zero)."
  type        = number
  default     = 0
}

variable "cloud_run_max_instances" {
  description = "Maximum instances per revision."
  type        = number
  default     = 3
}

variable "cloud_run_concurrency" {
  description = "Max concurrent requests per instance."
  type        = number
  default     = 80
}

variable "cloud_run_port" {
  description = "Container listens on this port."
  type        = number
  default     = 8080
}

variable "cloud_run_allow_unauthenticated" {
  description = "If true, grants roles/run.invoker to allUsers (public HTTPS)."
  type        = bool
  default     = true
}
