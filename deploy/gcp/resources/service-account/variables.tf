variable "project_id" {
  description = "GCP project ID where APIs, Cloud Run, the bucket, secrets, and the runtime service account are created."
  type        = string
}

variable "deployment_name" {
  description = "Short deployment name prefix (lowercase, hyphens). Used to identify deployment"
  type        = string
}

variable "account_id" {
  description = "Service account short name"
  type        = string
}

variable "display_name" {
  description = "Service account name"
  type        = string
}