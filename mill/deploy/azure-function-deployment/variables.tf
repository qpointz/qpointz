variable "subscription_id" {
  description="Azure subscription id"
}

variable "location" {
  description = "Deployment location"
  default = "northeurope"
}

variable "app_name" {
  description = "Application name"
  default = "bck-func-feat-az-deploy-ndrf"
}
