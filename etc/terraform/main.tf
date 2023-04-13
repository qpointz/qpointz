terraform {
    required_providers {
      azurerm = {
        source = "hashicorp/azurerm"
        version = ">= 3.0.2"
      }
    }

    required_version = ">= 1.3.0"
}

locals {
    slug = "srfx-${var.deployment}-${var.env}-${var.branch}"
    idslug = join("",regexall("[[:alnum:]]+", local.slug))

    commonTags = {
        surface-deployment-name = "${var.deployment}"
        surface-deployment-slug = local.slug
        surface-env = "${var.env}"
        surface-branch = "${var.branch}"
    }
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "rg" {
    name = "rg-${local.slug}-main"    
    location = var.location        
    tags = local.commonTags
}

resource "azurerm_virtual_network" "vnet" {
    name = "vnet-${local.slug}-bone"
    location = azurerm_resource_group.rg.location
    resource_group_name = azurerm_resource_group.rg.name
    address_space = ["10.0.0.0/16"]
    dns_servers = ["10.0.0.4", "10.0.0.5"]
    tags = local.commonTags
    subnet {
        name = "default"    
        address_prefix = "10.0.1.0/24"        
    }
}

resource "azurerm_storage_account" "storageaccount" {
    name = substr("sa${local.idslug}",1,24)
    resource_group_name = azurerm_resource_group.rg.name
    location = azurerm_resource_group.rg.location
    account_replication_type = "LRS"
    account_tier = "Standard"
    account_kind = "StorageV2"
    
    tags = local.commonTags
    
    min_tls_version = "TLS1_2"
    enable_https_traffic_only = true
    public_network_access_enabled = false
    allow_nested_items_to_be_public = false
    is_hns_enabled = true
    blob_properties {
      
      delete_retention_policy {
        days = 3
      }
    }    
}

resource "azurerm_storage_container" "storage-container-config" {  
  storage_account_name = azurerm_storage_account.storageaccount.name
  name = "configs"  
}

resource "azurerm_storage_container" "storage-container-inbound" {  
  storage_account_name = azurerm_storage_account.storageaccount.name
  name = "inbound"
}

resource "azurerm_storage_container" "storage-container-outbound" {  
  storage_account_name = azurerm_storage_account.storageaccount.name
  name = "outbound"
}