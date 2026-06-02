variable "project_id" {
  description = "GCP project ID where APIs, Cloud Run, the bucket, secrets, and the runtime service account are created."
  type        = string
}

variable "labels" {
  description = "config labels"
  type        = any
}

variable "deployment_name" {
  description = "Short stack prefix (lowercase, hyphens). Used in Cloud Run service id, bucket name, secret ids, and runtime SA account_id segment."
  type        = string
}

variable "content" {
  description = "Config content"
  type        = any
}

variable "file_name" {
  description = "Configuration file name"
  type        = string
}

variable "file_mount_path" {
  description = "Configuration file mount path"
  type        = string
}