variable "project_id" {
  description = "GCP project ID where APIs, Cloud Run, the bucket, secrets, and the runtime service account are created."
  type        = string
}

variable "region" {
  description = "Region for the Cloud Run service and the GCS bucket (bucket location matches this value)."
  type        = string
}

variable "deployment_name" {
  description = "Short stack prefix (lowercase, hyphens). Used in Cloud Run service id, bucket name, secret ids, and runtime SA account_id segment."
  type        = string
}

variable "labels" {
  description = "Bucket labels"
  type        = any
}

variable "volumes" {
  description = "Volumes to be mouneted"
  type = list(object({
    kind        = string
    volume_name = string
    mount_path  = string

    #secret file mount
    secret_id   = optional(string),
    file_name   = optional(string),
    version     = optional(string, "latest")

  }))
}

variable "envs" {
  description = "Environment variables"
  type = list(object({
    kind      = string
    name      = string
    value     = string
  }))
}

variable "max_instance_request_concurrency" {
  description = "Maximum concurrent requests per instance (template.max_instance_request_concurrency)."
  type        = number
}

variable "min_instance_count" {
  description = "Minimum instances (0 allows scale-to-zero when idle)."
  type        = number
}

variable "max_instance_count" {
  description = "Maximum instances for the service revision."
  type        = number
}

variable "limits_cpu" {
  description = "CPU limit for the container (Cloud Run limit string, e.g. \"1\", \"0.5\", \"2\")."
  type        = string
}

variable "limits_memory" {
  description = "Memory limit for the container (e.g. \"512Mi\", \"1Gi\", \"2Gi\")."
  type        = string
}

variable "runtime_sa_email" {
  description = "Service account email"
  type        = string
}

variable "image" {
  description = "Docker image"
  type        = string
}