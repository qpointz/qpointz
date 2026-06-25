# -----------------------------------------------------------------------------
# Primary resources — resource group, logging, storage, Key Vault config, Container Apps.
# -----------------------------------------------------------------------------

data "azurerm_client_config" "current" {}

# Only created when storage_account_name is auto-generated with generation = "random".
resource "random_id" "storage_suffix" {
  count = var.storage_account_name == null && var.storage_account_name_generation == "random" ? 1 : 0

  byte_length = 3
}

resource "azurerm_resource_group" "main" {
  name     = "${local.deployment}-rg"
  location = var.location
  tags     = local.tags
}

########################################################################
#### Container Apps environment (requires Log Analytics)
########################################################################

resource "azurerm_log_analytics_workspace" "main" {
  name                = "${local.deployment}-law"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  sku                 = "PerGB2018"
  retention_in_days   = 30
  tags                = local.tags
}

resource "azurerm_container_app_environment" "main" {
  name                       = "${local.deployment}-aca-env"
  location                   = azurerm_resource_group.main.location
  resource_group_name        = azurerm_resource_group.main.name
  log_analytics_workspace_id = azurerm_log_analytics_workspace.main.id
  tags                       = local.tags
}

########################################################################
#### Storage — blob container for Flow data only (no file share / account key)
########################################################################

resource "azurerm_storage_account" "main" {
  name                     = local.storage_account_name
  resource_group_name      = azurerm_resource_group.main.name
  location                 = azurerm_resource_group.main.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  min_tls_version          = "TLS1_2"

  tags = local.tags

  # Pin storage_account_name in tfvars after first apply — changing deployment_name or
  # storage_account_name_generation replaces the account and orphan blob data.
  lifecycle {
    create_before_destroy = true
  }
}

# Flow reads tables from blobs under prefix data/ (see config/flow.tpl.yml).
resource "azurerm_storage_container" "data" {
  name                  = local.blob_container_name
  storage_account_id    = azurerm_storage_account.main.id
  container_access_type = "private"
}

########################################################################
#### Application config — Key Vault secrets (mounted via UAMI, no storage keys)
########################################################################

resource "azurerm_key_vault" "config" {
  name                       = local.key_vault_name
  location                   = azurerm_resource_group.main.location
  resource_group_name        = azurerm_resource_group.main.name
  tenant_id                  = data.azurerm_client_config.current.tenant_id
  sku_name                   = "standard"
  soft_delete_retention_days = 7
  purge_protection_enabled   = var.key_vault_purge_protection_enabled
  rbac_authorization_enabled = true

  tags = local.tags
}

# OpenTofu/Terraform principal must write secrets during apply (RBAC-enabled vault).
resource "azurerm_role_assignment" "deployer_kv_secrets_officer" {
  scope                = azurerm_key_vault.config.id
  role_definition_name = "Key Vault Secrets Officer"
  principal_id         = data.azurerm_client_config.current.object_id
}

resource "azurerm_key_vault_secret" "application_yml" {
  name         = "application-yml"
  value        = local.application_yml_content
  key_vault_id = azurerm_key_vault.config.id

  depends_on = [azurerm_role_assignment.deployer_kv_secrets_officer]
}

resource "azurerm_key_vault_secret" "flow_yml" {
  name         = "flow-yml"
  value        = local.flow_yml_content
  key_vault_id = azurerm_key_vault.config.id

  depends_on = [azurerm_role_assignment.deployer_kv_secrets_officer]
}

resource "azurerm_key_vault_secret" "auth_yml" {
  name         = "auth-yml"
  value        = local.auth_yml_content
  key_vault_id = azurerm_key_vault.config.id

  depends_on = [azurerm_role_assignment.deployer_kv_secrets_officer]
}

########################################################################
#### Runtime identity — blob + Key Vault access (no shared storage keys)
########################################################################

resource "azurerm_user_assigned_identity" "runtime" {
  name                = "${local.deployment}-runtime"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  tags                = local.tags
}

resource "azurerm_role_assignment" "runtime_blob_reader" {
  scope                = azurerm_storage_account.main.id
  role_definition_name = "Storage Blob Data Reader"
  principal_id         = azurerm_user_assigned_identity.runtime.principal_id
}

resource "azurerm_role_assignment" "runtime_kv_secrets_user" {
  scope                = azurerm_key_vault.config.id
  role_definition_name = "Key Vault Secrets User"
  principal_id         = azurerm_user_assigned_identity.runtime.principal_id
}

########################################################################
#### Container App — mill-service-minimal
########################################################################

resource "azurerm_container_app" "mill" {
  name                         = "${local.deployment}-aca"
  container_app_environment_id = azurerm_container_app_environment.main.id
  resource_group_name          = azurerm_resource_group.main.name
  revision_mode                = "Single"

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.runtime.id]
  }

  # Key Vault references — ACA secret names cannot contain '.'; files mount as {name} under /app/config.
  secret {
    name                = "application-yml"
    key_vault_secret_id = azurerm_key_vault_secret.application_yml.versionless_id
    identity            = azurerm_user_assigned_identity.runtime.id
  }

  secret {
    name                = "flow-yml"
    key_vault_secret_id = azurerm_key_vault_secret.flow_yml.versionless_id
    identity            = azurerm_user_assigned_identity.runtime.id
  }

  secret {
    name                = "auth-yml"
    key_vault_secret_id = azurerm_key_vault_secret.auth_yml.versionless_id
    identity            = azurerm_user_assigned_identity.runtime.id
  }

  # allow_unauthenticated=true → public HTTPS FQDN; false → internal ingress only.
  ingress {
    external_enabled = var.allow_unauthenticated
    target_port      = 8080
    transport        = "auto"

    traffic_weight {
      percentage      = 100
      latest_revision = true
    }
  }

  template {
    min_replicas = var.min_replicas
    max_replicas = var.max_replicas

    volume {
      name         = "mill-config"
      storage_type = "Secret"
    }

    container {
      name   = "mill"
      image  = var.image_version
      cpu    = var.container_cpu
      memory = var.container_memory

      # Selects the UAMI when DefaultAzureCredential runs inside the container.
      env {
        name  = "AZURE_CLIENT_ID"
        value = azurerm_user_assigned_identity.runtime.client_id
      }

      volume_mounts {
        name = "mill-config"
        path = "/app/config"
      }
    }
  }

  tags = local.tags

  depends_on = [
    azurerm_role_assignment.runtime_blob_reader,
    azurerm_role_assignment.runtime_kv_secrets_user,
    azurerm_key_vault_secret.application_yml,
    azurerm_key_vault_secret.flow_yml,
    azurerm_key_vault_secret.auth_yml,
  ]
}
