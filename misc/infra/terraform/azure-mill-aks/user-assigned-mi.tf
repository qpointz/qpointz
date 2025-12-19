# User Assigned Managed Identity for AKS
resource "azurerm_user_assigned_identity" "aks" {
  name                = "${var.cluster_name}-aks-identity"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  tags = var.tags
}

# Role assignment for the User Assigned Managed Identity to pull from ACR
resource "azurerm_role_assignment" "acr_uami_pull" {
  scope                = azurerm_container_registry.main.id
  role_definition_name = "AcrPull"
  principal_id         = azurerm_user_assigned_identity.aks.principal_id
}

