resource "azurerm_resource_group" "rg" {
  location = var.location
  name     = "${local.app_name_slug}-rg"
  tags = merge({
    "qp-component-id" = "deployment-rg"
  }, local.default_tags)
}

resource "azurerm_storage_account" "app_storage_account" {
  location = var.location
  resource_group_name = azurerm_resource_group.rg.name
  name = "${local.app_name_slug}sa"
  account_tier = "Standard"
  account_replication_type = "LRS"
  tags = merge({
    "qp-component-id" = "backend-function-sa"
  }, local.default_tags)
}

resource "azurerm_service_plan" "app_service_plan" {
  location            = var.location
  name                = "${local.app_name_slug}-serviceplan"
  os_type             = "Linux"
  resource_group_name = azurerm_resource_group.rg.name
  sku_name            = "B1"
  tags = merge({
    "qp-component-id" = "backend-service-plan"
  }, local.default_tags)
}

resource "azurerm_user_assigned_identity" "app_uami" {
  location            = var.location
  name                = "${local.app_name_slug}-uami"
  resource_group_name = azurerm_resource_group.rg.name
  tags = merge({
    "qp-component-id" = "runtime-uami"
  }, local.default_tags)
}

resource "azurerm_linux_function_app" "application" {
  name = "${local.app_name_slug}-app"
  location = var.location
  resource_group_name = azurerm_resource_group.rg.name
  service_plan_id     = azurerm_service_plan.app_service_plan.id
  storage_account_access_key = azurerm_storage_account.app_storage_account.primary_access_key
  storage_account_name = azurerm_storage_account.app_storage_account.name
  identity {
    type = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.app_uami.id]
  }
  site_config {
    always_on = false
    use_32_bit_worker = false
    application_stack {
      java_version = "17"
    }
  }
  tags = merge({
    "qp-component-id" = "backend-function"
  }, local.default_tags)
}

output "app" {
  value = azurerm_linux_function_app.application.name
}

output "app_resource_id" {
  value = azurerm_linux_function_app.application.id
}

output "rg" {
  value = azurerm_resource_group.rg.name
}
