
resource "google_storage_bucket" "bucket" {
  name                        = join("-", [var.bucket_name, var.context.deployment_name])
  location                    = var.context.region
  uniform_bucket_level_access = true
  labels                      = var.context.labels
  force_destroy               = var.context.allow_destroy
  hierarchical_namespace {
     enabled = true
  }
}

resource "google_storage_bucket_iam_member" "runtime_main_bucket_viewer" {
  bucket     = google_storage_bucket.bucket.name
  role       = "roles/storage.objectViewer"
  member     = var.context.runtime_sa.member
  depends_on = [google_storage_bucket.bucket]
}

resource "google_storage_bucket_object" "files" {
  for_each = {
    for v in var.objects :
    v.name => v
    if v.source != null
  }

  bucket = google_storage_bucket.bucket.name
  name   = each.value.name
  source = each.value.source
}

resource "google_storage_bucket_object" "contents" {
  for_each = {
    for v in var.objects :
    v.name => v
    if v.content != null
  }

  bucket  = google_storage_bucket.bucket.name
  name    = each.value.name
  content = each.value.content
}

output "bucket" {
  value = google_storage_bucket.bucket
}
