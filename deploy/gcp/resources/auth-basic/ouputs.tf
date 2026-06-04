output "auth" {
  value =  {
    env = var.auth.enable ? [] : []
    volumes = var.auth.enable ? try(module.auth_config[0].volumes, []) : []

    spring = {
      profiles = var.auth.enable ? ["auth-basic"] : []
      configs = var.auth.enable ? [
        templatefile("${path.module}/config/auth-basic.tpl.yml", {
          auth_enable       = var.auth.enable
          auth_basic_enable = var.auth.enable
          store             = var.auth.store == "jpa" ? "jpa" : "file:/app/config/auth/auth.yml"
        })] : []
    }
  }
}

