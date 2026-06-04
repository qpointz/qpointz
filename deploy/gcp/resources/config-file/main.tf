locals {
  config_name = replace(replace(var.file_name, ".", "_"), " ","_")
  secret_id = join("-", [var.deployment_name, "config-file", local.config_name])
}

resource "google_secret_manager_secret" "config" {
  secret_id = local.secret_id

  replication {
    auto {}
  }

  labels     = var.labels
}

resource "google_secret_manager_secret_version" "config" {
  secret      = google_secret_manager_secret.config.id
  secret_data = var.content
}

output "secret_id" {
  value = google_secret_manager_secret.config.secret_id
}

output "secret_volume" {
  value = {
    kind = "secret",
    volume_name = local.config_name,
    secret_id   = google_secret_manager_secret.config.id,
    file_name   = var.file_name,
    mount_path  = var.file_mount_path,
    version     = "latest"
  }
}

output "volumes" {
  value = [{
    volume_name = local.config_name,
    mount_path  = var.file_mount_path,
    secret = {
      id = google_secret_manager_secret.config.id,
      file_name = var.file_name,
      version   = "latest"
    }
  }]
}

output "secret_data" {
  value = google_secret_manager_secret_version.config.secret_data
}