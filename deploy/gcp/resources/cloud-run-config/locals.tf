locals {

  metadata_seeds = [
    for i,v in var.template.metadata.seeds:
    {
       source = v
       name = "${var.template.bucket.path}seeds/${i}_${basename(v)}"
       path = "gs://${var.template.bucket.name}/${var.template.bucket.path}seeds/${i}_${basename(v)}"
    }
  ]

  service_spring_configs = templatefile("${path.module}/config/config.tpl.yml", {
    application_name  = var.template.app.name
    project_id        = var.context.project
    ui_enabled        = var.template.ui.enable
    metadata_seeds    = local.metadata_seeds
  })
}