# Mill Service Istio Chart

Helm chart for deploying Istio traffic management resources for Mill Service.

## Overview

This chart provides Istio-specific resources for managing traffic to an existing Mill Service deployment:
- **Gateway**: Exposes the service through Istio ingress gateway
- **VirtualService**: Defines routing rules for HTTP and gRPC traffic
- **DestinationRule**: Configures load balancing, circuit breaking, and connection pooling

## Key Features

- **Target Specific Deployments/Pods**: Route traffic to specific deployments or pods using labels and subsets
- **Canary Deployments**: Split traffic between different versions
- **HTTP and gRPC Support**: Full support for both protocols
- **Flexible Routing**: Route based on headers, paths, or other criteria
- **Circuit Breaking**: Built-in outlier detection and connection pooling

## Prerequisites

- Kubernetes cluster with Istio installed
- Existing `mill-service` deployment (deploy using `mill-service` chart first)
- `istioctl` or kubectl configured to access your cluster

## Installation

### Basic Installation

```bash
# First, deploy the service
helm install mill-service ./mill-service --namespace default

# Then, deploy Istio traffic management
helm install mill-service-istio ./mill-service-istio \
  --namespace default \
  --set service.name=mill-service
```

### Installation with Custom Configuration

```bash
helm install mill-service-istio ./mill-service-istio \
  --namespace default \
  --set service.name=mill-service \
  --set gateway.hosts[0]=mill.example.com \
  --set virtualService.hosts[0]=mill.example.com
```

### Installation with TLS

```bash
helm install mill-service-istio ./mill-service-istio \
  --namespace default \
  --set service.name=mill-service \
  --set gateway.hosts[0]=mill.example.com \
  --set gateway.ports[1].tls.credentialName=mill-tls-cert
```

## Configuration

### Service Reference

```yaml
service:
  name: mill-service      # Name of the Kubernetes Service
  namespace: default      # Namespace where service is deployed
  httpPort: 8080          # HTTP port
  grpcPort: 9090          # gRPC port
```

### Target Specific Deployments/Pods

You can target specific deployments or pods using labels:

```yaml
destination:
  # Option 1: Target by deployment name
  deploymentName: "mill-service"
  
  # Option 2: Target by pod labels
  podLabels:
    version: "v1"
    env: "production"
    app.kubernetes.io/instance: "mill-service"
```

When `podLabels` are specified, a subset will be automatically created in the DestinationRule.

### Gateway Configuration

```yaml
gateway:
  enabled: true
  selector:
    istio: ingressgateway
  hosts:
    - "mill.example.com"
  ports:
    - name: http
      number: 80
      protocol: HTTP
    - name: https
      number: 443
      protocol: HTTPS
      tls:
        mode: SIMPLE
        credentialName: "mill-tls-cert"
```

### VirtualService Configuration

```yaml
virtualService:
  enabled: true
  hosts:
    - "mill.example.com"
  http:
    - match:
        - uri:
            prefix: /
      route:
        - destination:
            host: mill-service
            subset: v1  # Route to specific subset
            port:
              number: 8080
          weight: 100
      timeout: 30s
      retries:
        attempts: 3
        perTryTimeout: 10s
  tcp:
    - match:
        - port: 9090
      route:
        - destination:
            host: mill-service
            subset: v1  # Route to specific subset
            port:
              number: 9090
```

### DestinationRule Configuration

```yaml
destinationRule:
  enabled: true
  host: mill-service
  trafficPolicy:
    loadBalancer:
      simple: ROUND_ROBIN
    connectionPool:
      tcp:
        maxConnections: 100
      http:
        http2MaxRequests: 100
    outlierDetection:
      consecutiveErrors: 3
      interval: 30s
  subsets:
    - name: v1
      labels:
        version: v1
    - name: v2
      labels:
        version: v2
```

## Multiple Deployments in Same Namespace

Yes, this chart can be used for multiple deployments in the same namespace. Each Helm release creates uniquely named resources.

### Option 1: Separate Gateways (Default)

Each deployment gets its own Gateway, VirtualService, and DestinationRule:

```bash
# Deploy first service
helm install mill-service-1 ./mill-service --namespace default
helm install mill-istio-1 ./mill-service-istio \
  --namespace default \
  --set service.name=mill-service-1

# Deploy second service
helm install mill-service-2 ./mill-service --namespace default
helm install mill-istio-2 ./mill-service-istio \
  --namespace default \
  --set service.name=mill-service-2 \
  --set gateway.hosts[0]=api2.example.com \
  --set virtualService.hosts[0]=api2.example.com
```

### Option 2: Shared Gateway (Recommended for Multiple Services)

Use a single shared Gateway and create separate VirtualServices for each service:

```bash
# Step 1: Create a shared Gateway (first deployment)
helm install shared-gateway ./mill-service-istio \
  --namespace default \
  --set service.name=mill-service-1 \
  --set gateway.enabled=true \
  --set virtualService.enabled=false \
  --set destinationRule.enabled=false \
  --set gateway.hosts[0]="*" \
  --set gateway.name=shared-gateway

# Step 2: Deploy first service with VirtualService only
helm install mill-service-1 ./mill-service --namespace default
helm install mill-istio-1 ./mill-service-istio \
  --namespace default \
  --set service.name=mill-service-1 \
  --set gateway.enabled=false \
  --set gateway.existingGateway=shared-gateway \
  --set virtualService.hosts[0]=api1.example.com

# Step 3: Deploy second service with VirtualService only
helm install mill-service-2 ./mill-service --namespace default
helm install mill-istio-2 ./mill-service-istio \
  --namespace default \
  --set service.name=mill-service-2 \
  --set gateway.enabled=false \
  --set gateway.existingGateway=shared-gateway \
  --set virtualService.hosts[0]=api2.example.com
```

### Option 3: Multiple Services, Same Gateway, Different Hosts

```yaml
# values-service1.yaml
service:
  name: mill-service-1
gateway:
  enabled: false
  existingGateway: shared-gateway
virtualService:
  hosts:
    - "api1.example.com"

# values-service2.yaml
service:
  name: mill-service-2
gateway:
  enabled: false
  existingGateway: shared-gateway
virtualService:
  hosts:
    - "api2.example.com"
```

### Important Notes for Multiple Deployments

1. **Resource Names**: Each release creates uniquely named resources based on the release name
   - Gateway: `{release-name}-gateway`
   - VirtualService: `{release-name}-vs`
   - DestinationRule: `{release-name}-dr`

2. **Gateway Sharing**: 
   - If multiple services share a Gateway, only create the Gateway once
   - Use `gateway.existingGateway` to reference the shared Gateway
   - Each service should have its own VirtualService

3. **Host Conflicts**: 
   - Ensure VirtualService hosts don't conflict
   - Use different domains or paths for each service

4. **Namespace**: 
   - All resources are created in the same namespace (configurable via `service.namespace`)
   - Gateway and VirtualService must be in the same namespace

## Examples

### Example 1: Basic HTTP and gRPC Routing

```yaml
service:
  name: mill-service
  namespace: default

gateway:
  enabled: true
  hosts:
    - "*"

virtualService:
  enabled: true
  hosts:
    - "*"
```

### Example 2: Target Specific Deployment by Labels

```yaml
service:
  name: mill-service
  namespace: default

destination:
  podLabels:
    version: "v1"
    app.kubernetes.io/instance: "mill-service"

destinationRule:
  subsets:
    - name: v1
      labels:
        version: "v1"
        app.kubernetes.io/instance: "mill-service"

virtualService:
  http:
    - route:
        - destination:
            host: mill-service
            subset: v1  # Route to v1 pods only
            port:
              number: 8080
          weight: 100
```

### Example 3: Canary Deployment (Split Traffic)

```yaml
destinationRule:
  subsets:
    - name: v1
      labels:
        version: "v1"
    - name: v2
      labels:
        version: "v2"

virtualService:
  http:
    - route:
        - destination:
            host: mill-service
            subset: v1
            port:
              number: 8080
          weight: 90  # 90% to v1
        - destination:
            host: mill-service
            subset: v2
            port:
              number: 8080
          weight: 10  # 10% to v2
```

### Example 4: Route Based on Headers to Different Deployments

```yaml
destinationRule:
  subsets:
    - name: v1
      labels:
        version: "v1"
    - name: v2
      labels:
        version: "v2"

virtualService:
  http:
    # Route to v1 for internal users
    - match:
        - headers:
            x-user-type:
              exact: internal
      route:
        - destination:
            host: mill-service
            subset: v1
            port:
              number: 8080
    # Route to v2 for external users
    - match:
        - headers:
            x-user-type:
              exact: external
      route:
        - destination:
            host: mill-service
            subset: v2
            port:
              number: 8080
    # Default route
    - route:
        - destination:
            host: mill-service
            subset: v1
            port:
              number: 8080
```

### Example 5: Production with TLS and Custom Domain

```yaml
service:
  name: mill-service
  namespace: production

gateway:
  enabled: true
  hosts:
    - "api.example.com"
  ports:
    - name: https
      number: 443
      protocol: HTTPS
      tls:
        mode: SIMPLE
        credentialName: "api-tls-cert"

virtualService:
  enabled: true
  hosts:
    - "api.example.com"
  http:
    - route:
        - destination:
            host: mill-service
            port:
              number: 8080
          weight: 100
      timeout: 60s
      retries:
        attempts: 5
        perTryTimeout: 15s
```

## Upgrading

```bash
helm upgrade mill-service-istio ./mill-service-istio \
  --namespace default \
  --set service.name=mill-service
```

## Uninstallation

```bash
helm uninstall mill-service-istio --namespace default
```

## Troubleshooting

### Check Gateway Status

```bash
kubectl get gateway -n default
kubectl describe gateway mill-service-istio-gateway -n default
```

### Check VirtualService

```bash
kubectl get virtualservice -n default
kubectl describe virtualservice mill-service-istio-vs -n default
```

### Check DestinationRule

```bash
kubectl get destinationrule -n default
kubectl describe destinationrule mill-service-istio-dr -n default
```

### Verify Service Exists

```bash
kubectl get svc mill-service -n default
```

### Verify Pod Labels Match Subsets

```bash
# Check pod labels
kubectl get pods -n default -l app.kubernetes.io/name=mill-service --show-labels

# Verify subset labels match pod labels
kubectl get destinationrule mill-service-istio-dr -n default -o yaml
```

### Test Traffic Flow

```bash
# Get ingress gateway IP
INGRESS_IP=$(kubectl get svc istio-ingressgateway -n istio-system -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

# Test HTTP endpoint
curl -H "Host: mill.example.com" http://$INGRESS_IP

# Test with header routing
curl -H "Host: mill.example.com" -H "x-user-type: internal" http://$INGRESS_IP
```

### Common Issues

**Issue: Traffic not reaching pods**

- Verify the Service selector matches pod labels
- Check that DestinationRule subset labels match pod labels
- Ensure VirtualService subset name matches DestinationRule subset name

**Issue: Subset not found**

- Verify pods have the labels specified in the DestinationRule subset
- Check that the Service selector includes those labels
- Ensure subset names match between VirtualService and DestinationRule

## Chart Values Reference

| Parameter | Description | Default |
|-----------|-------------|---------|
| `service.name` | Kubernetes Service name | `mill-service` |
| `service.namespace` | Service namespace | `default` |
| `destination.podLabels` | Pod labels for targeting | `{}` |
| `gateway.enabled` | Enable Gateway | `true` |
| `virtualService.enabled` | Enable VirtualService | `true` |
| `destinationRule.enabled` | Enable DestinationRule | `true` |

See `values.yaml` for all available options.

## Important Notes

1. **Service Selector Must Match**: The Service's selector must match the pod labels you use in subsets
2. **Labels Must Exist**: Pods must have the labels specified in the DestinationRule subsets
3. **Subset Names**: Subset names in VirtualService must match subset names in DestinationRule
4. **Multiple Deployments**: If you have multiple deployments for the same service, use distinct labels (e.g., `version`, `release`) and create subsets for each

## Support

For issues and questions:
- Verify the service is deployed and running
- Check Istio installation: `istioctl verify-install`
- Review Istio documentation: https://istio.io/latest/docs/
