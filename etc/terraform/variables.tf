variable "deployment" {
    description = "deployment name"
    type = string
    default = "ld"
}

variable "env" {
    description = "Environment"
    type = string
    default = "dev"
}

variable "branch" {
    description = "Deployment branch"
    type = string
    default = "local"
}

variable "location" {
    description = "Primary location"
    type = string
    default = "northeurope"
}