output "service" {
   value =  {
     env = []
     volumes = local.as_secret ? try(module.flow_config[0].volumes, []) : []
     spring = {
       profiles = var.flow.enable ? ["backend-flow"] : []
       configs = local.spring_config
     }
   }
}

