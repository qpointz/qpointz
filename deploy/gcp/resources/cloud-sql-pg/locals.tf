locals {
  service_spring_profiles = ["cloud-sql-pg"]

  envs                    = [
    {
      name      = "MILL_DB_PASSWORD"
      secret = {
        id = google_secret_manager_secret.dbpassword.secret_id
        version   = "latest"
      }
    }
  ]

  volumes                 = []

  service_spring_configs  = [
    templatefile("${path.module}/config/db-pg.tpl.yml", {
      MILL_DB_USERNAME  = google_sql_user.dbuser.name
      MILL_DB_HOST      = google_sql_database_instance.db.private_ip_address
      MILL_DB           = google_sql_database.db.name
    })
  ]
}