resource "google_storage_bucket_object" "data" {
  for_each = {
    for v in local.flow_sample_flat:
    v.name => v.file
  }

  bucket   = var.bucket_name
  name     = each.key
  source   = each.value
}