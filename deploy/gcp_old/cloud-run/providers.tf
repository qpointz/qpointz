# ADC: gcloud auth application-default login, or GOOGLE_APPLICATION_CREDENTIALS.
provider "google" {
  project = var.gcp_project_id
  region  = var.gcp_region
}
