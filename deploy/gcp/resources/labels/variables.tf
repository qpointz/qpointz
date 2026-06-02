variable "deployment_stack_name" {
  description = "Short stack prefix (lowercase, hyphens). Used to identify stack deployed"
  type        = string
}

variable "deployment_name" {
  description = "Short deployment name prefix (lowercase, hyphens). Used to identify deployment"
  type        = string
}

variable "mill_version" {
  description = "Mill version to deploy"
  type        = string
}