module "labels" {
  source                = "../../resources/labels"
  deployment_name       = var.deployment_name
  deployment_stack_name = var.deployment_stack_name
  mill_version          = var.mill_version
}

module "runtime-sa" {
  source                = "../../resources/service-account"
  project_id            = var.project_id
  deployment_name       = var.deployment_name
  account_id            = "runtime"
  display_name          = "Mill Runtime Service account"
  depends_on            = [google_project_service.apis]
}

module "bucket" {
  source                = "../../resources/bucket"
  project_id            = var.project_id
  region                = var.region
  deployment_name       = var.deployment_name
  force_destroy         = true
  bucket_name           = "data"
  labels                = module.labels.labels
  depends_on            = [google_project_service.apis]
}

module "app_config" {
  source           =  "../../resources/config-file"
  project_id       = var.project_id
  deployment_name  = var.deployment_name
  content          = templatefile(
      "${path.module}/config/application.tpl.yml",
      {
          application_name     = join("-", ["mill", var.deployment_name])
          schema_cache_enabled = var.schema_cache_enabled ? "true" : "false"
          schema_cache_ttl     = var.schema_cache_ttl
      })
  file_name        = "appplication.yml"
  file_mount_path  = "/app/config/"
  labels           = module.labels.labels
  depends_on       = [google_project_service.apis]
}

module "flow_config" {
  source           =  "../../resources/config-file"
  project_id       = var.project_id
  deployment_name  = var.deployment_name
  content          = templatefile(
    "${path.module}/config/flow.tpl.yml",
    {
      schema_name = var.schema_name
      bucket_name = module.bucket.bucket_name
      project_id  = var.project_id
    }
  )
  file_name        = "flow.yml"
  file_mount_path  = "/app/config/flow/"
  labels           = module.labels.labels
  depends_on       = [google_project_service.apis]
}

module "service" {
  source = "../../resources/cloud-run"
  project_id = var.project_id
  deployment_name = var.deployment_name
  region = var.region
  labels = module.labels.labels
  volumes = [
    module.app_config.secret_volume,
    module.flow_config.secret_volume
  ]
  envs = [
  ]
  runtime_sa_email = module.runtime-sa.service_account_email
  max_instance_count = var.service_max_instance_count
  min_instance_count = var.service_min_instance_count
  max_instance_request_concurrency = var.service_max_instance_request_concurrency
  limits_cpu = var.service_limits_cpu
  limits_memory = var.service_limits_memory
  image = join(":", ["qpointz/mill-service-minimal", local.mill_version_tag])

  depends_on = [
    module.app_config,
    module.flow_config,
    module.bucket,
    module.runtime-sa
  ]

}

# module "service_updated" {
#   source = "../../resources/cloud-run"
#   project_id = var.project_id
#   deployment_name = var.deployment_name
#   region = var.region
#   labels = module.labels.labels
#   volumes = [
#     module.app_config.secret_volume,
#     module.flow_config.secret_volume
#   ]
#   envs = [
#     # {
#     #   kind  = "static"
#     #   name  = "MILL_EXTERNAL_HOST_NAME"
#     #   value = regex("^https?://([^/]+)", module.service.uri)[0]
#     # },
#     # {
#     #   kind  = "static"
#     #   name  = "MILL_EXTERNAL_HOST_SCHEME"
#     #   value = regex("^([a-zA-Z][a-zA-Z0-9+.-]*)://", module.service.uri)[0]
#     # }
#   ]
#   runtime_sa_email = module.runtime-sa.service_account_email
#   max_instance_count = var.service_max_instance_count
#   min_instance_count = var.service_min_instance_count
#   max_instance_request_concurrency = var.service_max_instance_request_concurrency
#   limits_cpu = var.service_limits_cpu
#   limits_memory = var.service_limits_memory
#   image = join(":", ["qpointz/mill-service-minimal", local.mill_version_tag])
#
#   depends_on = [
#     module.service
#   ]
#
# }