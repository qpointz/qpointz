# -----------------------------------------------------------------------------
# Primary resources — APIs, storage, secrets, Cloud Run service.
# -----------------------------------------------------------------------------

resource "google_project_service" "apis" {
  for_each = toset([
    "run.googleapis.com",
    "secretmanager.googleapis.com",
    "storage.googleapis.com",
    "cloudresourcemanager.googleapis.com",
    "iam.googleapis.com",
  ])

  project            = var.project_id
  service            = each.key
  disable_on_destroy = false
}

resource "google_service_account" "runtime_sa" {
  project      = var.project_id
  account_id   = join("-", [local.deployment, "runtime"])
  display_name = "Mill (${local.deployment}) Service Account"
}


resource "google_storage_bucket" "main_bucket" {
  name                        = join("-", [local.deployment, "bucket"])
  location                    = var.region
  uniform_bucket_level_access = true
  labels                      = local.labels
  depends_on                  = [google_project_service.apis]
  force_destroy               = true
}

resource "google_storage_bucket_iam_member" "runtime_main_bucket_viewer" {
  bucket     = google_storage_bucket.main_bucket.name
  role       = "roles/storage.objectViewer"
  member     = "serviceAccount:${google_service_account.runtime_sa.email}"
  depends_on = [google_storage_bucket.main_bucket]
}


########################################################################
#### Application Config
########################################################################
resource "google_secret_manager_secret" "application_config" {
  secret_id = join("-", [local.deployment, "application-yml"])

  replication {
    auto {}
  }

  labels     = local.labels
  depends_on = [google_project_service.apis]
}

resource "google_secret_manager_secret_version" "application_config" {
  secret = google_secret_manager_secret.application_config.id
  secret_data = templatefile(
    "${path.module}/config/application.tpl.yml",
    {}
  )
}

resource "google_secret_manager_secret_iam_member" "application_config" {
  project   = var.project_id
  secret_id = google_secret_manager_secret.application_config.secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.runtime_sa.email}"
}


########################################################################
#### Flow Configuration
########################################################################
resource "google_secret_manager_secret" "flow_config" {
  secret_id = join("-", [local.deployment, "flow-yml"])
  replication {
    auto {}
  }
  labels     = local.labels
  depends_on = [google_project_service.apis, google_storage_bucket.main_bucket]
}

resource "google_secret_manager_secret_iam_member" "flow_config" {
  project   = var.project_id
  secret_id = google_secret_manager_secret.flow_config.secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.runtime_sa.email}"
}

resource "google_secret_manager_secret_version" "flow_config" {
  secret = google_secret_manager_secret.flow_config.id
  secret_data = templatefile(
    "${path.module}/config/flow.tpl.yml",
    {
      schema_name = "minimal"
      bucket_name = google_storage_bucket.main_bucket.name
      project_id  = var.project_id
    }
  )
}

########################################################################
#### Auth Configuration
########################################################################
resource "google_secret_manager_secret" "auth_config" {
  secret_id = join("-", [local.deployment, "auth-yml"])
  replication {
    auto {}
  }
  labels     = local.labels
  depends_on = [google_project_service.apis]
}

resource "google_secret_manager_secret_iam_member" "auth_config" {
  project   = var.project_id
  secret_id = google_secret_manager_secret.auth_config.secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.runtime_sa.email}"
}

resource "google_secret_manager_secret_version" "auth_config" {
  secret = google_secret_manager_secret.auth_config.secret_id
  secret_data = templatefile(
    "${path.module}/config/auth.tpl.yml",
    {}
  )
}


resource "google_cloud_run_v2_service" "mill" {
  name     = join("-", [local.deployment, "run-service"])
  location = var.region
  deletion_protection = false
  ingress = "INGRESS_TRAFFIC_ALL"

  template {
    service_account                  = google_service_account.runtime_sa.email
    max_instance_request_concurrency = 100

    scaling {
      min_instance_count = 0
      max_instance_count = 3
    }

    volumes {
      name = "mill-application-yml"
      secret {
        secret = google_secret_manager_secret.application_config.id
        items {
          path    = "application.yml"
          version = "latest"
        }
      }
    }

    volumes {
      name = "flow-yml"
      secret {
        secret = google_secret_manager_secret.flow_config.id
        items {
          path = "flow.yml"
          version = "latest"
        }
      }
    }

    volumes {
      name = "auth-yml"
      secret {
        secret = google_secret_manager_secret.auth_config.id
        items {
          path = "auth.yml"
          version = "latest"
        }
      }
    }

    containers {
      image = "qpointz/mill-test-complete:latest"

      volume_mounts {
        mount_path = "/app/config/"
        name       = "mill-application-yml"
      }

      volume_mounts {
        mount_path = "/app/config/flow/"
        name       = "flow-yml"
      }

      volume_mounts {
        mount_path = "/app/config/auth/"
        name       = "auth-yml"
      }

      ports {
        container_port = 8080
      }

      resources {
        limits = {
          cpu    = 1
          memory = "2Gi"
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

    labels = local.labels
  }

  labels = local.labels

  depends_on = [
    google_project_service.apis,
    google_service_account.runtime_sa,
    google_storage_bucket.main_bucket,
    google_secret_manager_secret.application_config,
    google_secret_manager_secret.flow_config,
    google_secret_manager_secret.auth_config,
    google_secret_manager_secret_version.flow_config,
    google_secret_manager_secret_version.application_config,
    google_secret_manager_secret_version.auth_config
  ]
}

resource "google_cloud_run_v2_service_iam_member" "invoker_public" {
  project  = var.project_id
  location = google_cloud_run_v2_service.mill.location
  name     = google_cloud_run_v2_service.mill.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}
