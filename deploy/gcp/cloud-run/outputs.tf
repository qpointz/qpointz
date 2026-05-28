output "cloud_run_uri" {
  description = "HTTPS URL for the Cloud Run service."
  value       = google_cloud_run_v2_service.mill.uri
}

output "runtime_service_account_email" {
  description = "Identity used by Cloud Run (managed SA, explicit SA, or default Compute SA)."
  value       = module.runtime_sa.runtime_sa_email
}

output "gcs_bucket_name" {
  description = "Config bucket managed by this stack, when configured."
  value       = trimspace(var.gcs_bucket_name) != "" ? var.gcs_bucket_name : null
}

output "secret_db_password_id" {
  description = "Secret Manager secret id for database password."
  value       = google_secret_manager_secret.db_password.secret_id
}

output "gcp_project_id" {
  description = "Deployed GCP project (for sync-bucket.sh / gcloud)."
  value       = var.gcp_project_id
}
