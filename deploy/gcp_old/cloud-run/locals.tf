# Root-only locals; shared label + runtime SA logic lives in ../modules/.
locals {
  gcs_location = trimspace(var.gcs_bucket_location) != "" ? var.gcs_bucket_location : var.gcp_region

  resolved_gcs_project_id = trimspace(var.mill_cloud_gcp_gcs_project_id) != "" ? var.mill_cloud_gcp_gcs_project_id : var.gcp_project_id

  application_yml_payload = trimspace(var.custom_application_yml_file) != "" ? file(var.custom_application_yml_file) : var.custom_application_yml_content

  mount_application_yml = trimspace(local.application_yml_payload) != ""

  secret_db_password_id = "mill-${var.cloud_run_service_name}-db-password"
  secret_openai_id      = "mill-${var.cloud_run_service_name}-openai-key"
  secret_application_id = "mill-${var.cloud_run_service_name}-application-yml"
}
