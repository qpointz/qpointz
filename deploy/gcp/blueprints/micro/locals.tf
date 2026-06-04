

locals {
  mill_version_tag = replace(var.mill_version, "-","")

  context = {
    project = var.project_id
    region  = var.region
    labels = {
      mill-stack      = var.deployment_stack_name
      mill-deployment = var.deployment_name
      mill-version    = replace(replace(var.mill_version, ".", "-"), " ", "-")
    }
    deployment_name = var.deployment_name
    allow_destroy   = true
    runtime_sa      = module.runtime-sa.service_account
  }

  service_spring_profiles = concat(
      module.app_config.service.spring.profiles
    , module.auth_basic.auth.spring.profiles
    , module.flow.service.spring.profiles
    , module.db.db_conn.service.service_spring_profiles
    , module.ai.service.spring.profiles
  )

  service_spring_configs = concat(
    module.app_config.service.spring.configs,
    module.auth_basic.auth.spring.configs,
    module.flow.service.spring.configs,
    module.db.db_conn.service.service_spring_configs
    , module.ai.service.spring.configs
  )

  service_image = join(":", ["qpointz/mill-service-complete", local.mill_version_tag])

  service_env = concat([],
     module.app_config.service.env
    ,module.db.db_conn.service.envs
    ,module.auth_basic.auth.env
    ,module.flow.service.env
    ,module.ai.service.envs
  )

  service_volumes = concat([],
     module.app_config.service.volumes
    ,module.flow.service.volumes
    ,module.db.db_conn.service.volumes
    ,module.auth_basic.auth.volumes
    ,module.ai.service.volumes
  )

}

