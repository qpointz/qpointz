# Create AKS Cluster
resource "azurerm_kubernetes_cluster" "main" {
  name                = var.cluster_name
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  dns_prefix          = var.cluster_name
  kubernetes_version  = var.kubernetes_version

  # User Assigned Managed Identity
  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.aks.id]
  }

  # Azure RBAC for Kubernetes Authorization
  azure_rbac_enabled = var.azure_rbac_enabled
  rbac_aad_enabled   = var.azure_rbac_enabled

  # Network Configuration
  network_profile {
    network_plugin    = "azure"
    load_balancer_sku = "standard"
    
    # Use Azure CNI with overlay for pod networking
    network_policy = var.enable_network_policy ? "azure" : null
  }

  # Default Node Pool
  default_node_pool {
    name                = "system"
    node_count          = var.default_node_pool_count
    vm_size             = var.default_node_pool_vm_size
    type                = "VirtualMachineScaleSets"
    enable_auto_scaling = var.enable_auto_scaling
    
    min_count = var.enable_auto_scaling ? var.auto_scaling_min_count : null
    max_count = var.enable_auto_scaling ? var.auto_scaling_max_count : null
    
    os_disk_size_gb     = var.default_node_pool_os_disk_size
    os_disk_type        = "Managed"
    zones               = var.node_pool_zones
    vnet_subnet_id      = azurerm_subnet.aks_nodes.id
    
    timeouts {
      create = "1h"
      update = "1h"
      delete = "1h"
    }
  }

  # OMS Agent / Azure Monitor
  oms_agent {
    enabled                    = true
    log_analytics_workspace_id = azurerm_log_analytics_workspace.main.id
  }

  # Azure Policy for Kubernetes
  azure_policy_enabled = var.enable_azure_policy

  # HTTP Application Routing (optional)
  http_application_routing_enabled = var.enable_http_application_routing

  tags = var.tags
}

# Additional Application Node Pool
resource "azurerm_kubernetes_cluster_node_pool" "applications" {
  count                  = var.create_additional_node_pool ? 1 : 0
  name                   = "applications"
  kubernetes_cluster_id  = azurerm_kubernetes_cluster.main.id
  vm_size                = var.additional_node_pool_vm_size
  node_count             = var.additional_node_pool_count
  enable_auto_scaling    = var.enable_auto_scaling
  
  min_count = var.enable_auto_scaling ? var.auto_scaling_min_count : null
  max_count = var.enable_auto_scaling ? var.auto_scaling_max_count : null
  
  os_disk_size_gb        = var.additional_node_pool_os_disk_size
  os_disk_type           = "Managed"
  zones                  = var.node_pool_zones
  vnet_subnet_id         = azurerm_subnet.aks_nodes.id
  priority               = "Regular"
  
  labels = {
    pool = "applications"
  }

  timeouts {
    create = "1h"
    update = "1h"
    delete = "1h"
  }
}

