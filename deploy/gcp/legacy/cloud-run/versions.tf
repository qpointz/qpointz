# -----------------------------------------------------------------------------
# Mill on GCP Cloud Run — OpenTofu root module (deploy/gcp/cloud-run).
# Post-apply: optional ./sync-bucket.sh for Skymill YAML objects in GCS.
# -----------------------------------------------------------------------------
terraform {
  required_version = ">= 1.6.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = ">= 5.40.0"
    }
  }
}
