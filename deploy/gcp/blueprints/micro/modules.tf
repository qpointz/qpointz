module "runtime-sa" {
  source                = "../../resources/service-account"
  deployment_name       = var.deployment_name
  account_id            = "runtime"
  display_name          = "Mill Runtime Service account"
  depends_on            = [google_project_service.apis]
}

module "bucket" {
  source                = "../../resources/bucket"
  bucket_name           = "data"
  depends_on            = [google_project_service.apis]
  context               = local.context
}

module "network" {
  source = "../../resources/network"
  deployment_name = var.deployment_name
}

module "db" {
  source = "../../resources/cloud-sql-pg"
  deployment_name = var.deployment_name
  labels          = local.context.labels
  runtime_sa  = module.runtime-sa.service_account
  network_id = module.network.network_id
  depends_on = [
    module.network
  ]
}

module "app_config" {
  source           = "../../resources/cloud-run-config"
  context = local.context
  template = {
    app = {
      name = join("-", ["mill", var.deployment_stack_name, var.deployment_name])
    }
    ui = {
      enable = true
    }
    bucket = {
      name = module.bucket.bucket.name
      path = "config/"
    }
    metadata = {
      seeds = var.metadata_seeds
    }

  }
}

module "flow" {
  source           = "../../resources/backend-flow"
  context = local.context
  flow = {
    enable = var.backend == "flow"
    as_secret = var.backend_flow_config_as_secret
    bucket = {
      name = module.bucket.bucket.name
      path = "config/flow/"
    }
    schema = {
      name = var.schema_name
      cache = {
        enable = var.schema_cache_enabled
        ttl = var.schema_cache_ttl
      }
    }
  }
}

module "service" {
  source = "../../resources/cloud-run-service"
  context = local.context
  template = {
    spring = {
      configs = local.service_spring_configs
      profiles = local.service_spring_profiles
    }
    service = {
      mill_version = var.mill_version
      image = local.service_image
      max_instance_request_concurrency = var.service_max_instance_request_concurrency
      scaling = {
        min_instance_count = var.service_min_instance_count
        max_instance_count = var.service_max_instance_count
      }
      limits = {
        cpu = var.service_limits_cpu
        memory = var.service_limits_memory
      }
      vpc_interface = {
        network = module.network.network_id
        subnetwork = module.network.subnet_id
      }
      volumes = local.service_volumes
      env = local.service_env
    }
  }
}

module "auth_basic" {
  source = "../../resources/auth-basic"
  context = local.context
  auth = {
    enable = var.auth_enable && var.auth_basic_enable
    store = var.auth_basic_store
    seed_users = var.auth_basic_seed_users
  }
}

module "initial_data" {
  source           = "../../resources/data-flow"
  context = local.context
  bucket_name = module.bucket.bucket.name
  data = var.flow_sample
}

module "ai" {
  source           = "../../resources/ai-config"
  context = local.context
  ai = {
    enable = var.ai_enable
  }
  openai = {
    key = var.ai_openai_key
    model = var.ai_openai_model
    embedding = {
      model = var.ai_openai_embed_model
      dimension = var.ai_openai_embed_dim
    }
  }
}