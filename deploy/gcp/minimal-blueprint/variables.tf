# --- Required core ---
variable "project_id" {
  description = "GCP project ID (must exist unless you provision it elsewhere)."
  type        = string
}

variable "region" {
  description = "Cloud Run region."
  type        = string
}

variable "deployment_name" {
  description = "Deployment name"
  type        = string
  default     = "mill-minimal-blpr"
}