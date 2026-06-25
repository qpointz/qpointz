# -----------------------------------------------------------------------------
# Provider configuration
# -----------------------------------------------------------------------------

# Auth: az login (Azure CLI) or ARM_* environment variables / OIDC in CI.
provider "azurerm" {
  features {
    resource_group {
      # Allow tofu destroy to remove the RG even when child resources still exist (dev stacks).
      prevent_deletion_if_contains_resources = false
    }
  }

  subscription_id = var.subscription_id
}

provider "azapi" {}
