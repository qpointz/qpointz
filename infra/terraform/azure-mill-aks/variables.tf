variable "resource_group_name" {
  description = "Name of the Azure Resource Group"
  type        = string
  default     = "rg-aks-mill"
}

variable "location" {
  description = "Azure region where resources will be deployed"
  type        = string
  default     = "eastus"
}

variable "cluster_name" {
  description = "Name of the AKS cluster"
  type        = string
  default     = "aks-mill-cluster"
}

variable "kubernetes_version" {
  description = "Kubernetes version to use for the AKS cluster"
  type        = string
  default     = "1.28"
}

variable "vnet_address_space" {
  description = "Address space for the virtual network"
  type        = string
  default     = "10.0.0.0/16"
}

variable "nodes_subnet_address_prefix" {
  description = "Address prefix for the AKS nodes subnet"
  type        = string
  default     = "10.0.1.0/24"
}

variable "pods_subnet_address_prefix" {
  description = "Address prefix for the AKS pods subnet"
  type        = string
  default     = "10.0.2.0/24"
}

variable "acr_name" {
  description = "Name of Azure Container Registry"
  type        = string
  default     = "acrmill"
  validation {
    condition     = can(regex("^[a-z0-9]+$", var.acr_name))
    error_message = "ACR name must be lowercase alphanumeric only and between 5-50 characters."
  }
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default = {
    Environment = "Development"
    Project     = "QPointz"
    ManagedBy   = "Terraform"
  }
}

# AKS Configuration
variable "azure_rbac_enabled" {
  description = "Enable Azure RBAC for Kubernetes Authorization"
  type        = bool
  default     = true
}

variable "enable_network_policy" {
  description = "Enable network policy for the AKS cluster"
  type        = bool
  default     = false
}

variable "enable_azure_policy" {
  description = "Enable Azure Policy for Kubernetes"
  type        = bool
  default     = true
}

variable "enable_http_application_routing" {
  description = "Enable HTTP Application Routing addon"
  type        = bool
  default     = false
}

# Default Node Pool Configuration
variable "default_node_pool_count" {
  description = "Number of nodes in the default node pool"
  type        = number
  default     = 2
}

variable "default_node_pool_vm_size" {
  description = "VM size for the default node pool"
  type        = string
  default     = "Standard_B2s"
}

variable "default_node_pool_os_disk_size" {
  description = "OS disk size in GB for the default node pool"
  type        = number
  default     = 30
}

# Auto Scaling Configuration
variable "enable_auto_scaling" {
  description = "Enable auto-scaling for node pools"
  type        = bool
  default     = true
}

variable "auto_scaling_min_count" {
  description = "Minimum node count for auto-scaling"
  type        = number
  default     = 2
}

variable "auto_scaling_max_count" {
  description = "Maximum node count for auto-scaling"
  type        = number
  default     = 5
}

# Additional Node Pool Configuration
variable "create_additional_node_pool" {
  description = "Create an additional node pool for applications"
  type        = bool
  default     = true
}

variable "additional_node_pool_count" {
  description = "Number of nodes in the additional node pool"
  type        = number
  default     = 2
}

variable "additional_node_pool_vm_size" {
  description = "VM size for the additional node pool"
  type        = string
  default     = "Standard_B2s"
}

variable "additional_node_pool_os_disk_size" {
  description = "OS disk size in GB for the additional node pool"
  type        = number
  default     = 30
}

variable "node_pool_zones" {
  description = "Availability zones for node pools"
  type        = list(string)
  default     = ["1", "2", "3"]
}

