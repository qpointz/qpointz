# -----------------------------------------------------------------------------
# Terraform / OpenTofu version and provider pins
# -----------------------------------------------------------------------------

terraform {
  required_version = ">= 1.6.0"

  required_providers {
    # Azure resources (Container Apps, storage, identity, RBAC).
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 4.0.0"
    }
    # Optional random storage account suffix (storage_account_name_generation = "random").
    random = {
      source  = "hashicorp/random"
      version = ">= 3.6.0"
    }
    # Java Development Stack (runtime.java, serviceBinds) — not in azurerm_container_app yet.
    azapi = {
      source  = "Azure/azapi"
      version = ">= 2.0.0"
    }
    null = {
      source  = "hashicorp/null"
      version = ">= 3.2.0"
    }
  }
}
