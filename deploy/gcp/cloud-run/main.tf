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

  project            = var.gcp_project_id
  service            = each.key
  disable_on_destroy = false
}

resource "google_storage_bucket" "config" {
  count = trimspace(var.gcs_bucket_name) != "" ? 1 : 0

  name                        = var.gcs_bucket_name
  location                    = local.gcs_location
  uniform_bucket_level_access = true

  labels = module.labels.labels

  depends_on = [google_project_service.apis]
}

resource "google_storage_bucket_iam_member" "runtime_object_viewer" {
  count = trimspace(var.gcs_bucket_name) != "" ? 1 : 0

  bucket = google_storage_bucket.config[0].name
  role   = "roles/storage.objectViewer"
  member = "serviceAccount:${module.runtime_sa.runtime_sa_email}"

  depends_on = [google_storage_bucket.config]
}

resource "google_secret_manager_secret" "db_password" {
  secret_id = local.secret_db_password_id

  replication {
    auto {}
  }

  labels = module.labels.labels

  depends_on = [google_project_service.apis]
}

resource "google_secret_manager_secret_version" "db_password" {
  secret      = google_secret_manager_secret.db_password.id
  secret_data = var.mill_db_password
}

resource "google_secret_manager_secret_iam_member" "db_password_accessor_runtime" {
  project   = var.gcp_project_id
  secret_id = google_secret_manager_secret.db_password.secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${module.runtime_sa.runtime_sa_email}"
}

resource "google_secret_manager_secret_iam_member" "db_password_accessor_robot" {
  project   = var.gcp_project_id
  secret_id = google_secret_manager_secret.db_password.secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${module.runtime_sa.serverless_robot_sa}"
}

resource "google_secret_manager_secret" "openai" {
  count = trimspace(var.openai_api_key) != "" ? 1 : 0

  secret_id = local.secret_openai_id

  replication {
    auto {}
  }

  labels = module.labels.labels

  depends_on = [google_project_service.apis]
}

resource "google_secret_manager_secret_version" "openai" {
  count = trimspace(var.openai_api_key) != "" ? 1 : 0

  secret      = google_secret_manager_secret.openai[0].id
  secret_data = var.openai_api_key
}

resource "google_secret_manager_secret_iam_member" "openai_accessor_runtime" {
  count = trimspace(var.openai_api_key) != "" ? 1 : 0

  project   = var.gcp_project_id
  secret_id = google_secret_manager_secret.openai[0].secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${module.runtime_sa.runtime_sa_email}"
}

resource "google_secret_manager_secret_iam_member" "openai_accessor_robot" {
  count = trimspace(var.openai_api_key) != "" ? 1 : 0

  project   = var.gcp_project_id
  secret_id = google_secret_manager_secret.openai[0].secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${module.runtime_sa.serverless_robot_sa}"
}

resource "google_secret_manager_secret" "application_yml" {
  count = local.mount_application_yml ? 1 : 0

  secret_id = local.secret_application_id

  replication {
    auto {}
  }

  labels = module.labels.labels

  depends_on = [google_project_service.apis]
}

resource "google_secret_manager_secret_version" "application_yml" {
  count = local.mount_application_yml ? 1 : 0

  secret      = google_secret_manager_secret.application_yml[0].id
  secret_data = local.application_yml_payload
}

resource "google_secret_manager_secret_iam_member" "application_yml_accessor_runtime" {
  count = local.mount_application_yml ? 1 : 0

  project   = var.gcp_project_id
  secret_id = google_secret_manager_secret.application_yml[0].secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${module.runtime_sa.runtime_sa_email}"
}

resource "google_secret_manager_secret_iam_member" "application_yml_accessor_robot" {
  count = local.mount_application_yml ? 1 : 0

  project   = var.gcp_project_id
  secret_id = google_secret_manager_secret.application_yml[0].secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${module.runtime_sa.serverless_robot_sa}"
}

resource "google_cloud_run_v2_service" "mill" {
  name     = var.cloud_run_service_name
  location = var.gcp_region

  ingress = "INGRESS_TRAFFIC_ALL"

  template {
    service_account                  = module.runtime_sa.runtime_sa_email
    max_instance_request_concurrency = var.cloud_run_concurrency

    scaling {
      min_instance_count = var.cloud_run_min_instances
      max_instance_count = var.cloud_run_max_instances
    }

    annotations = trimspace(var.cloud_sql_connection_name) != "" ? {
      "run.googleapis.com/cloudsql-instances" = var.cloud_sql_connection_name
    } : {}

    dynamic "volumes" {
      for_each = local.mount_application_yml ? [1] : []
      content {
        name = "mill-application-yml"
        secret {
          secret = google_secret_manager_secret.application_yml[0].id
          items {
            path    = "application.yml"
            version = "latest"
          }
        }
      }
    }

    containers {
      image = var.mill_docker_image

      ports {
        container_port = var.cloud_run_port
      }

      resources {
        limits = {
          cpu    = var.cloud_run_cpu
          memory = var.cloud_run_memory
        }
      }

      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = var.spring_profiles_active
      }

      env {
        name  = "MILL_DB_URL"
        value = var.mill_db_url
      }

      env {
        name  = "MILL_DB_USERNAME"
        value = var.mill_db_username
      }

      env {
        name = "MILL_DB_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_password.name
            version = "latest"
          }
        }
      }

      dynamic "env" {
        for_each = trimspace(var.openai_api_key) != "" ? [1] : []
        content {
          name = "OPENAI_API_KEY"
          value_source {
            secret_key_ref {
              secret  = google_secret_manager_secret.openai[0].name
              version = "latest"
            }
          }
        }
      }

      dynamic "env" {
        for_each = local.mount_application_yml ? [1] : []
        content {
          name  = "SPRING_CONFIG_ADDITIONAL_LOCATION"
          value = "file:${var.mill_runtime_config_mount_path}/application.yml"
        }
      }

      dynamic "env" {
        for_each = trimspace(var.gcs_bucket_name) != "" ? [1] : []
        content {
          name  = "GCS_BUCKET_NAME"
          value = var.gcs_bucket_name
        }
      }

      dynamic "env" {
        for_each = trimspace(var.gcs_bucket_name) != "" ? [1] : []
        content {
          name  = "MILL_CLOUD_GCP_GCS_PROJECT_ID"
          value = local.resolved_gcs_project_id
        }
      }

      dynamic "volume_mounts" {
        for_each = local.mount_application_yml ? [1] : []
        content {
          name       = "mill-application-yml"
          mount_path = var.mill_runtime_config_mount_path
        }
      }
    }

    labels = module.labels.labels
  }

  labels = module.labels.labels

  depends_on = [
    google_project_service.apis,
    module.runtime_sa,
    google_secret_manager_secret_version.db_password,
    google_secret_manager_secret_iam_member.db_password_accessor_runtime,
    google_secret_manager_secret_iam_member.db_password_accessor_robot,
  ]
}

resource "google_cloud_run_v2_service_iam_member" "invoker_public" {
  count = var.cloud_run_allow_unauthenticated ? 1 : 0

  project  = var.gcp_project_id
  location = google_cloud_run_v2_service.mill.location
  name     = google_cloud_run_v2_service.mill.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}
