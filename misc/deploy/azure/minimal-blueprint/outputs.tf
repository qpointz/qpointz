# -----------------------------------------------------------------------------
# Stack outputs — URLs, storage names, identity IDs for upload and troubleshooting.
# -----------------------------------------------------------------------------

########################################################################
#### Container App
########################################################################

output "container_app_url" {
  description = "HTTPS URL for the Container App (latest revision FQDN)."
  value       = "https://${azurerm_container_app.mill.ingress[0].fqdn}"
}

output "container_app_fqdn" {
  description = "FQDN for the Container App ingress."
  value       = azurerm_container_app.mill.ingress[0].fqdn
}

output "container_app_name" {
  description = "Container App resource name."
  value       = azurerm_container_app.mill.name
}

output "health_check_url" {
  description = "Spring Boot actuator health endpoint."
  value       = "https://${azurerm_container_app.mill.ingress[0].fqdn}/actuator/health"
}

########################################################################
#### Identity
########################################################################

output "runtime_identity_principal_id" {
  description = "Principal ID of the user-assigned managed identity used by the Container App."
  value       = azurerm_user_assigned_identity.runtime.principal_id
}

output "runtime_identity_client_id" {
  description = "Client ID of the user-assigned managed identity (for Azure SDK / DefaultAzureCredential)."
  value       = azurerm_user_assigned_identity.runtime.client_id
}

########################################################################
#### Storage — Flow data layout: {blob_container}/{blob_data_prefix}{table}/file.ext
########################################################################

output "storage_account_name" {
  description = "Storage account for Flow data blobs."
  value       = azurerm_storage_account.main.name
}

output "blob_container_name" {
  description = "Blob container for Flow data (objects under blob_data_prefix)."
  value       = azurerm_storage_container.data.name
}

output "blob_data_prefix" {
  description = "Blob prefix scanned by the Flow descriptor (include trailing slash when building paths)."
  value       = local.blob_data_prefix
}

output "key_vault_name" {
  description = "Key Vault holding Mill config secrets (application-yml, flow-yml, auth-yml)."
  value       = azurerm_key_vault.config.name
}

output "key_vault_uri" {
  description = "Key Vault URI for config secrets."
  value       = azurerm_key_vault.config.vault_uri
}

########################################################################
#### Deploy metadata
########################################################################

output "flow_schema_name" {
  description = "Flow / Calcite schema name from config/flow.tpl.yml (var.schema_name)."
  value       = var.schema_name
}

output "subscription_id" {
  description = "Deployed Azure subscription."
  value       = var.subscription_id
}

output "location" {
  description = "Azure region for the stack."
  value       = var.location
}

output "deployment_name" {
  description = "Stack prefix used for resource names."
  value       = local.deployment
}

output "resource_group_name" {
  description = "Resource group containing all stack resources."
  value       = azurerm_resource_group.main.name
}

########################################################################
#### Java Development Stack
########################################################################

output "java_admin_component_name" {
  description = "Admin for Spring Java component name (when enable_admin_for_spring is true)."
  value       = var.enable_admin_for_spring ? var.java_admin_component_name : null
}

output "java_admin_dashboard_url" {
  description = "HTTPS URL for the Admin for Spring dashboard (requires Microsoft.App/managedEnvironments/write on the environment to open in portal)."
  value       = var.enable_admin_for_spring ? try("https://${azapi_resource.admin_for_spring[0].output}", null) : null
}

output "java_development_stack_enabled" {
  description = "Whether Java runtime settings were applied to the Container App."
  value       = var.enable_java_development_stack
}
