//flow as bucket
resource "google_storage_bucket_object" "flow-config" {
  count    = local.as_object ? 1 : 0
  bucket   = var.flow.bucket.name
  name     = "${var.flow.bucket.path}${local.source_file}"
  content  = local.source_content
}

//flow as secret
module "flow_config" {
  count    = local.as_secret ? 1 : 0

  source           =  "../config-file"
  deployment_name  = var.context.deployment_name
  content          = local.source_content
  file_name        = local.source_file
  file_mount_path  = local.source_file_mount
  labels           = var.context.labels
}

resource "google_secret_manager_secret_iam_member" "flow_config_runtime" {
  count    = local.as_secret ? 1 : 0
  secret_id = module.flow_config[0].secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = var.context.runtime_sa.member
}