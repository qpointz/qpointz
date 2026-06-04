output "project" {
  value = var.project_id
}

output "region" {
  value = var.region
}

# output "seed_user" {
#   sensitive = true
#   value =  local.seed_users
# }

output "bucket_name" {
  value = module.bucket.bucket.name
}

output "sql" {
  sensitive = true
  value = module.db.db_conn
}

