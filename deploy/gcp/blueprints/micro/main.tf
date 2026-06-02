terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = ">= 7.23.0"
    }
  }
}
# -----------------------------------------------------------------------------
# Primary resources — APIs, storage, secrets, Cloud Run service.
# -----------------------------------------------------------------------------

resource "google_project_service" "apis" {
  for_each = toset([
    "run.googleapis.com",
    "secretmanager.googleapis.com",
    "storage.googleapis.com",
    "cloudresourcemanager.googleapis.com",
    "iam.googleapis.com",
  ])

  project            = var.project_id
  service            = each.key
  disable_on_destroy = false
}

# resource "google_storage_folder" "seeds" {
#   bucket = module.bucket.bucket_name
#   name   = "seeds/"
# }
#
# resource "google_storage_folder" "data" {
#   bucket = module.bucket.bucket_name
#   name   = "data/"
# }

resource "google_storage_bucket_iam_member" "runtime_main_bucket_viewer" {
  bucket     = module.bucket.bucket_name
  role       = "roles/storage.objectViewer"
  member     = module.runtime-sa.service_account_member
  depends_on = [module.bucket, module.runtime-sa]
}

resource "google_secret_manager_secret_iam_member" "application_config_runtime" {
  project   = var.project_id
  secret_id = module.app_config.secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = module.runtime-sa.service_account_member
}

resource "google_secret_manager_secret_iam_member" "flow_config_runtime" {
  project   = var.project_id
  secret_id = module.flow_config.secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = module.runtime-sa.service_account_member
}

resource "google_cloud_run_v2_service_iam_member" "invoker_public" {
  project  = var.project_id
  location = module.service.location
  name     = module.service.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}