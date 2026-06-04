locals {
  volumes = concat(var.template.service.volumes,
    module.app_config.volumes
  )
}