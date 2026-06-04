output "service" {
   value =  {
     env = []
     volumes = []
     spring = {
       profiles = ["cloud-run", "gcp"]
       configs = [local.service_spring_configs]
     }
   }
}

