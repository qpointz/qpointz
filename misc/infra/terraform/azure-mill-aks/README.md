# Azure AKS Terraform Configuration

This Terraform configuration provisions a complete Azure Kubernetes Service (AKS) infrastructure on Azure.

## Architecture Overview

The infrastructure includes:
- **Resource Group** - Container for all resources
- **Virtual Network** - Networking foundation with subnets
  - **Nodes Subnet** - For AKS node pools
  - **Pods Subnet** - For Kubernetes pods
- **Azure Kubernetes Service (AKS)** - Managed Kubernetes cluster
  - Default system node pool
  - Optional additional applications node pool
- **Azure Container Registry (ACR)** - Private container registry
- **Log Analytics Workspace** - For AKS monitoring and insights
- **RBAC** - Azure RBAC for Kubernetes authorization

## Prerequisites

1. **Azure CLI** installed and configured
   ```bash
   az login
   az account set --subscription "your-subscription-id"
   ```

2. **Terraform** installed (>= 1.0)
   ```bash
   terraform version
   ```

3. **Azure Subscription** with appropriate permissions:
   - Contributor role or specific resource permissions
   - Ability to create AKS clusters, ACR, and networking resources

## Quick Start

### 1. Configure Variables

Copy the example variables file and customize as needed:

```bash
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` with your specific values:

```hcl
resource_group_name = "rg-aks-mill-production"
location            = "eastus"
cluster_name        = "aks-mill-prod"
acr_name            = "acrmillprod"
```

### 2. Initialize Terraform

```bash
terraform init
```

### 3. Review the Plan

```bash
terraform plan
```

### 4. Apply Configuration

```bash
terraform apply
```

Confirm when prompted. The deployment typically takes 15-20 minutes.

## Configuration Options

### Network Configuration

Default network configuration:
- VNet: `10.0.0.0/16`
- Nodes Subnet: `10.0.1.0/24`
- Pods Subnet: `10.0.2.0/24`

Customize in `variables.tf` or `terraform.tfvars`.

### Node Pools

#### Default Node Pool
- Purpose: System workloads
- Default: 2 nodes, `Standard_B2s` VMs

#### Applications Node Pool
- Purpose: Application workloads
- Default: 2 nodes, `Standard_B2s` VMs
- Can be disabled by setting `create_additional_node_pool = false`

### Auto Scaling

Auto-scaling is enabled by default:
- Min nodes: 2
- Max nodes: 5

Adjust in variables as needed.

### Features

Toggleable features:
- **Azure RBAC** - Kubernetes authorization via Azure AD
- **Network Policy** - Kubernetes network policy enforcement
- **Azure Policy** - Policy management and compliance
- **HTTP Application Routing** - Ingress controller addon
- **OMS Agent** - Container insights and monitoring

## Post-Deployment

### Connect to AKS Cluster

```bash
# Get credentials
az aks get-credentials --resource-group <resource-group-name> --name <cluster-name>

# Verify connection
kubectl get nodes
```

### Push Images to ACR

```bash
# Login to ACR
az acr login --name <acr-name>

# Tag and push image
docker tag <image:tag> <acr-name>.azurecr.io/<image:tag>
docker push <acr-name>.azurecr.io/<image:tag>

# Pull from AKS
kubectl run --image=<acr-name>.azurecr.io/<image:tag> <pod-name>
```

### View Logs and Monitoring

```bash
# View cluster insights in Azure Portal
az aks browse --resource-group <resource-group-name> --name <cluster-name>

# Or access Log Analytics directly
az monitor log-analytics workspace show --workspace-name <workspace-name> --resource-group <resource-group-name>
```

## Common Commands

```bash
# Refresh state
terraform refresh

# Show current state
terraform show

# Output specific values
terraform output aks_cluster_name
terraform output kube_config_raw

# Destroy infrastructure (CAUTION!)
terraform destroy
```

## Important Notes

### ACR Integration
The AKS cluster is automatically granted pull permissions to ACR through managed identity. No additional configuration needed.

### Cost Optimization
- Use appropriate VM sizes for your workload
- Consider spot instances for dev/test environments
- Enable auto-scaling to optimize node utilization
- Use `Standard_B2s` for dev/test, larger SKUs for production

### Security Best Practices
- Enable Azure RBAC for authentication
- Use Azure Policy for compliance
- Enable network policies for micro-segmentation
- Regularly update Kubernetes version
- Use managed identities instead of service principles

### State Management
Consider using remote state backends:
- Azure Storage Account with blob storage
- Terraform Cloud
- HashiCorp Consul

Example backend configuration:
```hcl
terraform {
  backend "azurerm" {
    resource_group_name  = "rg-terraform-state"
    storage_account_name = "terraformstate"
    container_name       = "tfstate"
    key                  = "aks-mill.terraform.tfstate"
  }
}
```

## Troubleshooting

### Common Issues

**Issue**: Terraform times out during apply
- **Solution**: Increase timeout values in node pool configuration

**Issue**: ACR pull fails from AKS
- **Solution**: Verify role assignment was created successfully

**Issue**: Subnet delegation error
- **Solution**: Ensure subnet has proper delegation to AKS

**Issue**: Insufficient permissions
- **Solution**: Verify service principal/identity has Contributor role

## Variables Reference

See `variables.tf` for complete variable documentation.

### Key Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `resource_group_name` | Resource group name | `rg-aks-mill` |
| `location` | Azure region | `eastus` |
| `cluster_name` | AKS cluster name | `aks-mill-cluster` |
| `kubernetes_version` | Kubernetes version | `1.28` |
| `acr_name` | ACR name (lowercase alphanumeric) | `acrmill` |
| `default_node_pool_vm_size` | Node VM size | `Standard_B2s` |
| `enable_auto_scaling` | Enable auto-scaling | `true` |

## Next Steps

After deployment:
1. Configure ingress controller (if not using HTTP Application Routing)
2. Set up CI/CD pipelines
3. Configure application namespaces and RBAC
4. Deploy monitoring dashboards
5. Set up backup and disaster recovery
6. Configure auto-scaling for pods (HPA/VPA)

## Support

For issues or questions:
- Terraform Azure Provider: https://registry.terraform.io/providers/hashicorp/azurerm
- AKS Documentation: https://docs.microsoft.com/azure/aks
- Azure Support: https://azure.microsoft.com/support

## License

This configuration is provided as-is for use in your Azure environment.

