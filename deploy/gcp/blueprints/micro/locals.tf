locals {
  volumes = [
    module.app_config.secret_volume,
    module.flow_config.secret_volume
  ]

  mill_version_tag = replace(var.mill_version, "-","")
}