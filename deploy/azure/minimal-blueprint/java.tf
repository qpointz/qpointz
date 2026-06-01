# -----------------------------------------------------------------------------
# Java Development Stack — Admin for Spring component, JVM metrics, Java agent.
# azurerm_container_app does not support configuration.runtime.java or
# template.serviceBinds; azapi_update_resource applies them after each apply.
# See: https://learn.microsoft.com/en-us/azure/container-apps/java-admin
#      https://learn.microsoft.com/en-us/azure/container-apps/java-metrics
# -----------------------------------------------------------------------------

########################################################################
#### Admin for Spring (managed Java component in the ACA environment)
########################################################################

resource "azapi_resource" "admin_for_spring" {
  count = var.enable_admin_for_spring ? 1 : 0

  type      = "Microsoft.App/managedEnvironments/javaComponents@2024-10-02-preview"
  name      = var.java_admin_component_name
  parent_id = azurerm_container_app_environment.main.id

  body = {
    properties = {
      componentType = "SpringBootAdmin"
      ingress       = {}
      scale = {
        minReplicas = var.java_admin_min_replicas
        maxReplicas = var.java_admin_max_replicas
      }
    }
  }

  schema_validation_enabled = false
  response_export_values    = ["properties.ingress.fqdn"]

  depends_on = [azurerm_container_app_environment.main]
}

# Unbind before Admin component delete on full destroy (Azure returns 409 otherwise).
resource "null_resource" "admin_unbind_guard" {
  count = var.enable_admin_for_spring ? 1 : 0

  triggers = {
    app_name = azurerm_container_app.mill.name
    rg_name  = azurerm_resource_group.main.name
    bind     = var.java_admin_component_name
  }

  provisioner "local-exec" {
    when    = destroy
    command = "az containerapp update --name ${self.triggers.app_name} --resource-group ${self.triggers.rg_name} --unbind ${self.triggers.bind}"
  }

  depends_on = [azapi_resource.admin_for_spring[0]]
}

########################################################################
#### Container App — service binds (must clear before Admin component delete)
########################################################################

resource "azapi_update_resource" "mill_service_binds" {
  type        = "Microsoft.App/containerApps@2024-10-02-preview"
  resource_id = azurerm_container_app.mill.id

  body = {
    properties = {
      template = {
        serviceBinds = var.enable_admin_for_spring ? [
          {
            name      = var.java_admin_component_name
            serviceId = azapi_resource.admin_for_spring[0].id
          },
        ] : []
      }
    }
  }

  depends_on = [
    azurerm_container_app.mill,
    azapi_update_resource.mill_config_mounts,
  ]
}

########################################################################
#### Container App — Java runtime (metrics / optional Java agent)
########################################################################

resource "azapi_update_resource" "mill_java_runtime" {
  count = var.enable_java_development_stack ? 1 : 0

  type        = "Microsoft.App/containerApps@2024-10-02-preview"
  resource_id = azurerm_container_app.mill.id

  body = {
    properties = {
      configuration = {
        runtime = {
          java = {
            enableMetrics = var.java_enable_metrics
            javaAgent = {
              enabled = var.java_enable_java_agent
            }
          }
        }
      }
    }
  }

  depends_on = [
    azurerm_container_app.mill,
    azapi_update_resource.mill_config_mounts,
    azapi_update_resource.mill_service_binds,
  ]
}
