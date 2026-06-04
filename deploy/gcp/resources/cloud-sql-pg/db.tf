resource "google_sql_database_instance" "db" {
  deletion_protection = false
  name             = join("-", ["mill-db", var.deployment_name])
  database_version = "POSTGRES_16"
  settings {
    edition = "ENTERPRISE"
    tier = "db-f1-micro"
    ip_configuration {
      ipv4_enabled = false
      private_network = var.network_id
    }
  }
}

resource "google_sql_database" "db" {
  instance = google_sql_database_instance.db.name
  name     = "mill"
}

resource "random_password" "dbpassword" {
  length  = 32
  special = false
}

resource "google_sql_user" "dbuser" {
  instance = google_sql_database_instance.db.name
  name     = "mill"
  password = random_password.dbpassword.result
}

output "db_conn" {
  value = {
    db = {
      host     = google_sql_database_instance.db.private_ip_address
      database = google_sql_database.db.name
      port     = 5432
    }
    user = {
      name = google_sql_user.dbuser.name
      password_secret_id = google_secret_manager_secret.dbpassword.name
      password = google_sql_user.dbuser.password
    }
    service = {
      service_spring_profiles = local.service_spring_profiles,
      envs                    = local.envs
      volumes                 = local.volumes
      service_spring_configs  = local.service_spring_configs
    }

  }
}

