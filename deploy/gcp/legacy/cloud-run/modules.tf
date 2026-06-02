data "google_project" "current" {
  project_id = var.gcp_project_id
}

module "labels" {
  source = "../modules/mill-labels"

  service_name    = var.cloud_run_service_name
  deploy_stack_id = var.deploy_stack_id
}

module "runtime_sa" {
  source = "../modules/mill-runtime-sa"

  gcp_project_id                  = var.gcp_project_id
  project_number                  = data.google_project.current.number
  cloud_run_service_name          = var.cloud_run_service_name
  cloud_run_managed_runtime_sa    = var.cloud_run_managed_runtime_sa
  cloud_run_service_account_email = var.cloud_run_service_account_email
  gcs_bucket_name                 = var.gcs_bucket_name

  depends_on = [google_project_service.apis]
}
