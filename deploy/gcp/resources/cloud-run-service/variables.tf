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
  description = "Service Template"
  type = object({
    spring = object({
      configs = list(string)
      profiles = list(string)
    })
    service = object({
      image = string
      mill_version = string
      max_instance_request_concurrency = optional(number,100)
      scaling = optional(object({
        min_instance_count = optional(number,0)
        max_instance_count = optional(number,3)
      }), {})
      limits = optional(object({
        cpu = optional(string,"1")
        memory = optional(string, "2Gi")
      }), {})
      vpc_interface = object({
        network = string,
        subnetwork = string
      })
      volumes = optional(list(object({
          volume_name = string,
          mount_path  = optional(string),
          secret = optional(object({
            id = string,
            version = string
            file_name = string
          }),null)
      })),[])
      env = optional(list(object({
        name = string,
        value = optional(string, null)
        secret = optional(object({
          id = string
          version = string
        }), null)
      })),[])

    })
  })
}