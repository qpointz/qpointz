resource "google_cloud_run_v2_service" "mill" {
  name                = join("-", [var.deployment_name, "run-service"])
  location            = var.region
  deletion_protection = false
  ingress             = "INGRESS_TRAFFIC_ALL"
  project             = var.project_id

  template {
    service_account                  = var.runtime_sa_email
    max_instance_request_concurrency = var.max_instance_request_concurrency

    scaling {
      min_instance_count = var.min_instance_count
      max_instance_count = var.max_instance_count
    }

    dynamic "volumes" {
      for_each = [
         for v in var.volumes: v if v.kind == "secret"
      ]

      content {
        name = volumes.value.volume_name
        secret {
          secret = volumes.value.secret_id
          items {
            path = volumes.value.file_name
            version = volumes.value.version
          }
        }
      }
    }

    containers {
      image = var.image

      dynamic "volume_mounts" {
        for_each = var.volumes
        content {
          mount_path = volume_mounts.value.mount_path
          name       = volume_mounts.value.volume_name
        }
      }

      ports {
        container_port = 8080
      }

      resources {
        limits = {
          cpu    = var.limits_cpu
          memory = var.limits_memory
        }
      }

      dynamic "env" {
        for_each = [
          for v in var.envs: v if v.kind == "static"
        ]
        content {
          name = env.value.name
          value = env.value.value
        }
      }

      # env {
      #   name  = "SPRING_PROFILES_ACTIVE"
      #   value = var.spring_profiles_active
      # }
      #
      # env {
      #   name  = "MILL_DB_URL"
      #   value = var.mill_db_url
      # }
      #
      # env {
      #   name  = "MILL_DB_USERNAME"
      #   value = var.mill_db_username
      # }
      #
      # env {
      #   name = "MILL_DB_PASSWORD"
      #   value_source {
      #     secret_key_ref {
      #       secret  = google_secret_manager_secret.db_password.name
      #       version = "latest"
      #     }
      #   }
      # }
      #
      # dynamic "env" {
      #   for_each = trimspace(var.openai_api_key) != "" ? [1] : []
      #   content {
      #     name = "OPENAI_API_KEY"
      #     value_source {
      #       secret_key_ref {
      #         secret  = google_secret_manager_secret.openai[0].name
      #         version = "latest"
      #       }
      #     }
      #   }
      # }
      #
      # dynamic "env" {
      #   for_each = local.mount_application_yml ? [1] : []
      #   content {
      #     name  = "SPRING_CONFIG_ADDITIONAL_LOCATION"
      #     value = "file:${var.mill_runtime_config_mount_path}/application.yml"
      #   }
      # }
      #
      # dynamic "env" {
      #   for_each = trimspace(var.gcs_bucket_name) != "" ? [1] : []
      #   content {
      #     name  = "GCS_BUCKET_NAME"
      #     value = var.gcs_bucket_name
      #   }
      # }
      #
      # dynamic "env" {
      #   for_each = trimspace(var.gcs_bucket_name) != "" ? [1] : []
      #   content {
      #     name  = "MILL_CLOUD_GCP_GCS_PROJECT_ID"
      #     value = local.resolved_gcs_project_id
      #   }
      # }
    }

    labels = var.labels
  }

  labels = var.labels
}

output "name" {
  value = google_cloud_run_v2_service.mill.name
}

output "location" {
  value = google_cloud_run_v2_service.mill.location
}

output "uri" {
  value =  google_cloud_run_v2_service.mill.uri
}