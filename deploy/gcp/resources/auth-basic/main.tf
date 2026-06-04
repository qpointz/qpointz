resource "random_password" "user" {
  for_each = {
    for u in var.auth.seed_users :
    u.user => u
  }

  length  = 20
  special = false
}

locals {
   seed_users = [
      for u in var.auth.seed_users : {
        user           = u.user
        password_plain = u.password == "" ? random_password.user[u.user].result : u.password
        hash_algo      = "bcrypt"
        password_hash  = bcrypt(u.password == "" ? random_password.user[u.user].result : u.password)
        groups         = u.groups
      }
   ]
}

module "auth_config" {
  count     = (var.auth.enable && var.auth.store !="jpa") == true ? 1 : 0
  source           =  "../config-file"
  deployment_name  = var.context.deployment_name
  content          = templatefile(
    "${path.module}/config/auth.tpl.yml",
    {
      users = local.seed_users
    }
  )
  file_name        = "auth.yml"
  file_mount_path  = "/app/config/auth/"
  labels           = var.context.labels
}

resource "google_secret_manager_secret_iam_member" "auth_config_runtime" {
  count     = (var.auth.enable && var.auth.store !="jpa") ? 1 : 0
  secret_id = module.auth_config[0].secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = var.context.runtime_sa.member
  depends_on = [module.auth_config]
}