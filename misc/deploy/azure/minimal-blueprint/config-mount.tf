# -----------------------------------------------------------------------------
# Secret volume path mapping — ACA secret names cannot contain '.', but Spring
# and Mill expect application.yml, flow/flow.yml, auth/auth.yml (same as GCP).
# azurerm_container_app mounts secrets by secret name; azapi maps paths.
# -----------------------------------------------------------------------------

resource "azapi_update_resource" "mill_config_mounts" {
  type        = "Microsoft.App/containerApps@2024-10-02-preview"
  resource_id = azurerm_container_app.mill.id

  body = {
    properties = {
      template = {
        volumes = [
          {
            name        = "mill-config"
            storageType = "Secret"
            secrets = [
              {
                secretRef = "application-yml"
                path      = "application.yml"
              },
              {
                secretRef = "flow-yml"
                path      = "flow/flow.yml"
              },
              {
                secretRef = "auth-yml"
                path      = "auth/auth.yml"
              },
            ]
          },
        ]
      }
    }
  }

  depends_on = [azurerm_container_app.mill]
}
