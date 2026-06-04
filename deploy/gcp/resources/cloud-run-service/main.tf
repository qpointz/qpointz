module "app_config" {
  source           =  "../config-file"
  deployment_name  = var.context.deployment_name
  content          = join("\n---\n", var.template.spring.configs)
  file_name        = "application.yml"
  file_mount_path  = "/app/config/"
  labels           = var.context.labels
}


resource "google_secret_manager_secret_iam_member" "application_config_runtime" {
  secret_id = module.app_config.secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = var.context.runtime_sa.member
}


resource "google_cloud_run_v2_service" "mill" {
  name                = join("-", [var.context.deployment_name, "run-service"])
  location            = var.context.region
  deletion_protection = false
  ingress             = "INGRESS_TRAFFIC_ALL"

  template {
    service_account                  = var.context.runtime_sa.email
    max_instance_request_concurrency = var.template.service.max_instance_request_concurrency

    scaling {
     min_instance_count =  var.template.service.scaling.min_instance_count
     max_instance_count =  var.template.service.scaling.max_instance_count
    }

    vpc_access {
      network_interfaces {
        network = var.template.service.vpc_interface.network
        subnetwork = var.template.service.vpc_interface.subnetwork
      }
    }


    dynamic "volumes" {
      for_each = {
        for v in local.volumes:
        v.volume_name => v
        if try(v.secret,null) !=null
      }

      content {
        name = volumes.value.volume_name
        secret {
          secret = volumes.value.secret.id
          items {
            path = volumes.value.secret.file_name
            version = volumes.value.secret.version
          }
        }
      }
    }

    dynamic "volumes" {
      for_each = {
        for v in local.volumes:
        v.volume_name => v
        if try(v.cloudsql,null) !=null
      }
      content {
        name = volumes.value.volume_name
        cloud_sql_instance {
          instances = volumes.value.instances
        }
      }
    }


    containers {
      image = var.template.service.image

      liveness_probe {
        period_seconds = 30
        timeout_seconds = 10
        http_get {
          path  = "/.well-known/mill"
          port  = 8080
        }
      }

      startup_probe {
        initial_delay_seconds = 60
        timeout_seconds = 10
        period_seconds = 10
        failure_threshold = 10
        tcp_socket {
          port = 8080
        }
      }

      dynamic "volume_mounts" {
        for_each = {
          for v in local.volumes:
          v.volume_name => v
          if can(v.mount_path)
        }
        content {
          mount_path = volume_mounts.value.mount_path
          name       = volume_mounts.value.volume_name
        }
      }

      ports {
        container_port = 8080
      }

      resources {
        limits = var.template.service.limits
      }

      dynamic "env" {
        for_each = {
          for v in var.template.service.env :
          v => v
          if try(v.value, null)!=null
        }
        content {
          name = env.value.name
          value = env.value.value
        }
      }

      dynamic "env" {
        for_each = {
          for v in var.template.service.env :
          v.name => v
          if try(v.secret, null)!=null
        }

        content {
          name = env.value.name
          value_source {
            secret_key_ref {
              secret = env.value.secret.id
              version = env.value.secret.version
            }
          }
        }
      }

      env {
        name   = "SPRING_PROFILES_ACTIVE"
        value  = join(",", var.template.spring.profiles)
      }

      env {
        name    = "GOOGLE_CLOUD_PROJECT"
        value   = var.context.project
      }

      env {
        name    = "LOGGING_FILE_ENABLED"
        value   = "false"
      }

      env {
        name  = "SPRING_CONFIG_ADDITIONAL_LOCATION"
        value = "file:/app/config/application.yml"
      }
    }

    labels = var.context.labels
  }

  labels = var.context.labels
}

resource "google_cloud_run_v2_service_iam_member" "invoker_public" {
  location = google_cloud_run_v2_service.mill.location
  name     = google_cloud_run_v2_service.mill.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

output "service_uri" {
  value =  google_cloud_run_v2_service.mill.uri
}