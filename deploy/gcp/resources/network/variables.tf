variable "deployment_name" {
  description = "Short stack prefix (lowercase, hyphens). Used in Cloud Run service id, bucket name, secret ids, and runtime SA account_id segment."
  type        = string
  default     = "bp-micro-dep"
}

variable "subnet_cidr" {
  type    = string
  default = "10.10.0.0/24"
}

variable "psa_prefix_length" {
  type    = number
  default = 16
}