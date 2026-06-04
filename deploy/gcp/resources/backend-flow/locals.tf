locals {
  source_content = templatefile("${path.module}/config/flow.tpl.yml", {
    schema_name = var.flow.schema.name
    bucket_name = var.flow.bucket.name
    project_id  = var.context.project
  })

  source_file   = "flow.yml"
  source_file_mount = "/app/config/flow"

  as_secret = var.flow.enable && var.flow.as_secret == null
  as_object = var.flow.enable && !var.flow.as_secret != null

  spring_config = var.flow.enable ? [
    templatefile("${path.module}/config/backend-flow.tpl.yml", {
      schema_cache_enabled = var.flow.schema.cache.enable ? "true" : "false"
      schema_cache_ttl     = var.flow.schema.cache.ttl
      flow_config_path     = local.as_secret ? "${local.source_file_mount}/${local.source_file}" : "gs://${var.flow.bucket.name}/${var.flow.bucket.path}${local.source_file}"
    })] : []
}