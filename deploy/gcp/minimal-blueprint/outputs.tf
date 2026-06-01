output "cloud_run_uri" {
  description = "HTTPS URL for the Cloud Run service."
  value       = google_cloud_run_v2_service.mill.uri
}

output "cloud_run_service_name" {
  description = "Cloud Run service name (for gcloud run services …)."
  value       = google_cloud_run_v2_service.mill.name
}

output "runtime_service_account_email" {
  description = "Service account identity used by the Cloud Run revision."
  value       = google_service_account.runtime_sa.email
}

output "gcs_bucket_name" {
  description = "GCS bucket for Flow data (objects under gcs_data_prefix)."
  value       = google_storage_bucket.main_bucket.name
}

output "gcs_data_prefix" {
  description = "Object prefix scanned by the Flow descriptor (include trailing slash when building gs:// paths)."
  value       = "data/"
}

output "flow_schema_name" {
  description = "Calcite / Flow schema name from the deployed flow descriptor."
  value       = local.flow_schema_name
}

output "gcp_project_id" {
  description = "Deployed GCP project."
  value       = var.project_id
}

output "region" {
  description = "Cloud Run and bucket region."
  value       = var.region
}

output "deployment_name" {
  description = "Stack prefix used for resource names."
  value       = local.deployment
}

output "secret_application_yml_id" {
  description = "Secret Manager secret id for application.yml."
  value       = google_secret_manager_secret.application_config.secret_id
}

output "secret_flow_yml_id" {
  description = "Secret Manager secret id for flow.yml."
  value       = google_secret_manager_secret.flow_config.secret_id
}

output "secret_auth_yml_id" {
  description = "Secret Manager secret id for auth.yml."
  value       = google_secret_manager_secret.auth_config.secret_id
}

output "health_check_url" {
  description = "Spring Boot actuator health endpoint."
  value       = "${google_cloud_run_v2_service.mill.uri}/actuator/health"
}
