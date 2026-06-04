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

variable "template" {
  description = "FLow configutation"
  type = object({
    app = object({
      name = string
    })
    ui = optional(object({
      enable = optional(bool,false)
    }),{})
    bucket = optional(object({
      name = string
      path = optional(string, "config/")
    }), null)
    metadata = object({
      seeds = optional(list(string),[])
    })
  })
}