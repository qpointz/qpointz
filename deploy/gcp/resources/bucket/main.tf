resource "google_storage_bucket" "main_bucket" {
  name                        = join("-", [var.bucket_name, var.deployment_name])
  project                     = var.project_id
  location                    = var.region
  uniform_bucket_level_access = true
  labels                      = var.labels
  force_destroy               = var.force_destroy
  hierarchical_namespace {
     enabled = true
  }
}

output "bucket_name" {
  value = google_storage_bucket.main_bucket.name
}