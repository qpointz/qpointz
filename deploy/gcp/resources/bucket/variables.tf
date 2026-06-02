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

variable "bucket_name" {
  description = "Short bucket name"
  type        = string
}

variable "labels" {
  description = "Bucket labels"
  type        = any
}

variable "force_destroy" {
  description = "Force destroy bucket and all objects in bucket on destroy"
  type        = bool
}