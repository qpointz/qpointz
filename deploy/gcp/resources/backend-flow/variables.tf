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

variable "flow" {
  description = "FLow configutation"
  type = object({
    enable = bool
    as_secret = bool
    bucket = optional(object({
      name = string
      path = optional(string, "config/")
    }), null)
    schema = object({
      name = string
      cache = optional(object({
        enable = optional(bool, true)
        ttl = optional(string, "5m")
      }), {})
    })
  })
}