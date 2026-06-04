resource "random_password" "user" {
  for_each = {
    for u in var.auth_seed_users :
    u.user => u
  }

  length  = 20
  special = false
}

locals {
  volumes = [
    module.app_config.secret_volume,
    module.flow_config.secret_volume
  ]

  mill_version_tag = replace(var.mill_version, "-","")

  active_spring_profiles = join(",", concat(
    ["gcp"]
    , var.auth_enable ? ["auth"] : []
  ))

  seed_users = [
     for u in var.auth_seed_users : {
      user           = u.user
      password_plain = u.password == "" ? random_password.user[u.user].result : u.password
      hash_algo      = "noop" #"bcrypt"
      password_hash  = u.password == "" ? random_password.user[u.user].result : u.password #bcrypt(random_password.user[u.user].result)
      groups         = u.groups
    }
  ]
}