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

variable "auth" {
  description = "Basic auth parameters"
  type = object({
    enable = bool
    store  = string
    seed_users = optional(list(object({
      user = string
      password = optional(string, "")
      groups = optional(set(string), [])
    })),
      [
        {
          user     = "admin",
          password = "{noop}admin"
          groups = ["admin"]
        }
      ])
  })
}