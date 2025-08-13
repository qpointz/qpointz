locals {
  app_name_slug = substr(replace(var.app_name,"/\\W|_|\\s/",""),0,20)
  default_tags = {
    "qp-deployment-id" = var.deployment_id
  }
}