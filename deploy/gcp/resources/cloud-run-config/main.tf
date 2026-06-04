resource "google_storage_bucket_object" "seeds" {
  for_each = {
    for v in local.metadata_seeds:
    v.source => v.name
  }

  bucket   = var.template.bucket.name
  name     = each.value
  source   = each.key
}