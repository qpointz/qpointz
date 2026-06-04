# -----------------------------------------------------------------------------
# Naming — prefixes resource names: {deployment_name}-run-service, -bucket, -runtime, etc.
# -----------------------------------------------------------------------------

variable "deployment_name" {
  description = "Short stack prefix (lowercase, hyphens). Used in Cloud Run service id, bucket name, secret ids, and runtime SA account_id segment."
  type        = string
  default     = "bp-micro-dep"
}

variable "labels" {
  description = "Bucket labels"
  type        = any
}

variable "runtime_sa" {
  description = "Service account"
  type = object({
    email = string
    member = string
  })
}

variable "network_id" {
  type    = string
}