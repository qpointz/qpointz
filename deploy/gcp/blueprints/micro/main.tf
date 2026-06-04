terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = ">= 7.23.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.9"
    }
    postgresql = {
      source  = "cyrilgdn/postgresql"
      version = "~> 1.26"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}
# -----------------------------------------------------------------------------
# Primary resources — APIs, storage, secrets, Cloud Run service.
# -----------------------------------------------------------------------------

resource "google_project_service" "apis" {
  for_each = toset([
    "run.googleapis.com",
    "secretmanager.googleapis.com",
    "storage.googleapis.com",
    "cloudresourcemanager.googleapis.com",
    "iam.googleapis.com",
    "sql-component.googleapis.com",
    "vpcaccess.googleapis.com",
    "compute.googleapis.com",
    "servicenetworking.googleapis.com"
  ])

  project            = var.project_id
  service            = each.key
  disable_on_destroy = false
}

resource "google_storage_folder" "config" {
  bucket = module.bucket.bucket.name
  name   = "config/"
}

resource "google_storage_folder" "data" {
  bucket = module.bucket.bucket.name
  name   = "data/"
}