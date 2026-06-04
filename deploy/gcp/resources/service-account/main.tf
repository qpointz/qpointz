resource "google_service_account" "runtime_sa" {
  account_id   = join("-", [var.deployment_name, var.account_id])
  display_name = var.display_name
}

output "service_account_email" {
  value = google_service_account.runtime_sa.email
}

output "service_account_member" {
  value     = "serviceAccount:${google_service_account.runtime_sa.email}"
}

output "service_account" {
  value = {
    email = google_service_account.runtime_sa.email
    member = "serviceAccount:${google_service_account.runtime_sa.email}"
  }
}