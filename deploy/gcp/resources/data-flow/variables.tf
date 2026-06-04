variable "context" {
  description = "Deployment Context"
  type = object({
    labels          = optional(map(string),{})
    region          = string
    project         = string
    deployment_name = string
    allow_destroy   = optional(bool,false)
    runtime_sa      = object({
      email   = string
      member  = string
    })
  })
}

variable "bucket_name" {
  description = "bucket"
  type = string
}

variable "data" {
  type = map(list(string))
  default = {}
}