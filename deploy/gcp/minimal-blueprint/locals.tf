locals {
  labels = {
    mill-managed-by   = "managed_by"
    mill-deploy-stack = "minimal"
  }

  deployment = "${var.deployment_name}"
}
