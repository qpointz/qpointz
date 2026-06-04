output "service" {
  value =  {
    envs = [
      {
        name      = "OPENAI_API_KEY"
        secret = {
          id = google_secret_manager_secret.openai_key.secret_id
          version   = "latest"
        }
      }
    ]
    volumes = []
    spring = {
      profiles = var.ai.enable ? ["ai"] : [ ]
      configs = local.service_spring_configs
    }
  }
}