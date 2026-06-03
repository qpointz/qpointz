# ADC: gcloud auth application-default login, or GOOGLE_APPLICATION_CREDENTIALS.
provider "google" {
  project = var.project_id
  region  = var.region
}
