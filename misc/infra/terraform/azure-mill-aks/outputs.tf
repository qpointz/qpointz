output "resource_group_name" {
  description = "Name of the Azure Resource Group"
  value       = azurerm_resource_group.main.name
}

output "resource_group_location" {
  description = "Location of the Azure Resource Group"
  value       = azurerm_resource_group.main.location
}

output "vnet_name" {
  description = "Name of the Virtual Network"
  value       = azurerm_virtual_network.main.name
}

output "vnet_id" {
  description = "ID of the Virtual Network"
  value       = azurerm_virtual_network.main.id
}

output "aks_cluster_name" {
  description = "Name of the AKS cluster"
  value       = azurerm_kubernetes_cluster.main.name
}

output "aks_cluster_id" {
  description = "ID of the AKS cluster"
  value       = azurerm_kubernetes_cluster.main.id
}

output "aks_cluster_fqdn" {
  description = "FQDN of the AKS cluster"
  value       = azurerm_kubernetes_cluster.main.fqdn
}

output "aks_cluster_private_fqdn" {
  description = "Private FQDN of the AKS cluster"
  value       = azurerm_kubernetes_cluster.main.private_fqdn
}

output "kube_config_raw" {
  description = "Raw Kubernetes config"
  value       = azurerm_kubernetes_cluster.main.kube_config_raw
  sensitive   = true
}

output "host" {
  description = "Kubernetes cluster host"
  value       = azurerm_kubernetes_cluster.main.kube_config[0].host
}

output "client_key" {
  description = "Base64 encoded private key used by clients to authenticate to the cluster"
  value       = azurerm_kubernetes_cluster.main.kube_config[0].client_key
  sensitive   = true
}

output "client_certificate" {
  description = "Base64 encoded public certificate used by clients to authenticate to the cluster"
  value       = azurerm_kubernetes_cluster.main.kube_config[0].client_certificate
  sensitive   = true
}

output "cluster_ca_certificate" {
  description = "Base64 encoded public CA certificate used as the root of trust for the cluster"
  value       = azurerm_kubernetes_cluster.main.kube_config[0].cluster_ca_certificate
  sensitive   = true
}

output "cluster_identity" {
  description = "Managed identity of the AKS cluster"
  value       = azurerm_kubernetes_cluster.main.identity
}

output "user_assigned_identity_id" {
  description = "ID of the User Assigned Managed Identity for AKS"
  value       = azurerm_user_assigned_identity.aks.id
}

output "user_assigned_identity_client_id" {
  description = "Client ID of the User Assigned Managed Identity for AKS"
  value       = azurerm_user_assigned_identity.aks.client_id
}

output "user_assigned_identity_principal_id" {
  description = "Principal ID of the User Assigned Managed Identity for AKS"
  value       = azurerm_user_assigned_identity.aks.principal_id
}

output "acr_name" {
  description = "Name of Azure Container Registry"
  value       = azurerm_container_registry.main.name
}

output "acr_login_server" {
  description = "Login server of Azure Container Registry"
  value       = azurerm_container_registry.main.login_server
}

output "acr_id" {
  description = "ID of Azure Container Registry"
  value       = azurerm_container_registry.main.id
}

output "log_analytics_workspace_id" {
  description = "ID of the Log Analytics Workspace"
  value       = azurerm_log_analytics_workspace.main.id
}

output "log_analytics_workspace_name" {
  description = "Name of the Log Analytics Workspace"
  value       = azurerm_log_analytics_workspace.main.name
}

output "get_credentials_command" {
  description = "Command to get AKS credentials"
  value       = "az aks get-credentials --resource-group ${azurerm_resource_group.main.name} --name ${azurerm_kubernetes_cluster.main.name}"
}

