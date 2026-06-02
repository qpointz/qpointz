output "labels" {
  description = "Labels for deployment"

  value = {
    mill-stack      = var.deployment_stack_name
    mill-deployment = var.deployment_name
    mill-version    = replace(replace(var.mill_version, ".", "-"), " ", "-")
  }
}