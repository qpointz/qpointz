locals {

  password_secret_id = join("-", [var.deployment_name, "db-password"])

}

resource "google_secret_manager_secret" "dbpassword" {
  secret_id = local.password_secret_id
  replication {
    auto {}
  }
  labels     = var.labels
}

resource "google_secret_manager_secret_iam_member" "auth_config_runtime" {
  secret_id = google_secret_manager_secret.dbpassword.secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = var.runtime_sa.member
}


resource "google_secret_manager_secret_version" "dbpassword" {
  secret      = google_secret_manager_secret.dbpassword.id
  secret_data = google_sql_user.dbuser.password
}

