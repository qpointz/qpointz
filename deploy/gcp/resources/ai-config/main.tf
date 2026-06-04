locals {
  openai_key_secret_id = join("-", [var.context.deployment_name, "ai-open-ai-key"])

  service_spring_configs = var.ai.enable ? [templatefile("${path.module}/config/config.tpl.yml", {
    enable = var.ai.enable
    openai = var.openai
  })] : []

}

resource "google_secret_manager_secret" "openai_key" {
  secret_id = local.openai_key_secret_id
  replication {
    auto {}
  }
  labels     = var.context.labels
}

resource "google_secret_manager_secret_iam_member" "auth_config_runtime" {
  secret_id = google_secret_manager_secret.openai_key.secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = var.context.runtime_sa.member
}


resource "google_secret_manager_secret_version" "dbpassword" {
  secret      = google_secret_manager_secret.openai_key.id
  secret_data = var.openai.key
}