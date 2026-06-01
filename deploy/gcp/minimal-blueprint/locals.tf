locals {
  labels = {
    mill-managed-by   = "google-cloud-run"
    mill-deploy-stack = "minimal-blueprint"
  }

  deployment = var.deployment_name

  # Cloud Run control plane identity — needs secretAccessor to mount Secret volumes on revisions.
  serverless_robot_sa = "service-${data.google_project.current.number}@serverless-robot-prod.iam.gserviceaccount.com"
}

data "google_project" "current" {
  project_id = var.project_id
}
