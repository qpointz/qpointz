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

variable "ai" {
  type = object({
    enable= optional(bool,false)
  })
}

variable "openai" {
  sensitive = true
  type = object({
    key = string
    model = optional(string,"gpt-4o-mini")
    embedding = object({
      model = optional(string, "text-embedding-3-small")
      dimension = optional(number, 1536)
    })
  })
}