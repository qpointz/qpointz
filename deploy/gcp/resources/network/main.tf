resource "google_compute_network" "main" {
  name                    = "${var.deployment_name}-vpc"
  auto_create_subnetworks = false
}

resource "google_compute_subnetwork" "main" {
  name          = "${var.deployment_name}-subnet"
  network       = google_compute_network.main.id
  ip_cidr_range = var.subnet_cidr
}

resource "google_compute_global_address" "psa" {
  name          = "${var.deployment_name}-psa"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  prefix_length = var.psa_prefix_length

  network = google_compute_network.main.id
}

resource "google_service_networking_connection" "psa" {
  network = google_compute_network.main.id
  service = "servicenetworking.googleapis.com"

  reserved_peering_ranges = [
    google_compute_global_address.psa.name
  ]
}

# resource "google_vpc_access_connector" "cloudrun" {
#   name   = "${var.deployment_name}-connector"
#   network = google_compute_network.main.name
#   ip_cidr_range = "10.8.0.0/28"
# }