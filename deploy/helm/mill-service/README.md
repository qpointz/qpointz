# Mill Service Helm Chart

Helm chart for deploying Mill Service - a data access and NL-to-SQL platform.

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
  - [Image Configuration](#image-configuration)
  - [Application Configuration](#application-configuration)
  - [Secrets Management](#secrets-management)
  - [Command Line Arguments](#command-line-arguments)
- [Deployment](#deployment)
- [Updating and Restarting](#updating-and-restarting)
- [Accessing the Service](#accessing-the-service)
- [Troubleshooting](#troubleshooting)
- [Examples](#examples)

## Overview

Mill Service provides:
- **REST API** on port 8080 for HTTP access and web UI
- **gRPC API** on port 9099 for Mill protocol
- **NL-to-SQL** capabilities with AI integration
- **Metadata management** for data relations and annotations
- **Multiple backend providers** (JDBC, etc.)

## Prerequisites

- Kubernetes cluster (1.19+)
- Helm 3.x
- kubectl configured to access your cluster
- (Optional) Access to container registry for custom images

## Installation

### Basic Installation

```bash
cd deploy/helm
helm install mill-service ./mill-service --namespace default --create-namespace
```

### Installation with Custom Values

```bash
helm install mill-service ./mill-service \
  --namespace mill \
  --create-namespace \
  --set image.tag=v0.6.0 \
  --set replicaCount=2
```

### Installation from Values File

```bash
helm install mill-service ./mill-service \
  --namespace default \
  --values custom-values.yaml
```

## Configuration

### Image Configuration

Configure the container image in `values.yaml`:

```yaml
image:
  # Option 1: Use full image name (overrides repository:tag)
  name: qpointz/mill-service-samples:v0.6.0-dev
  
  # Option 2: Use repository and tag separately
  repository: qpointz/mill-service-samples
  tag: "v0.6.0-dev"
  
  pullPolicy: IfNotPresent
```

**Using a private registry:**

```yaml
imagePullSecrets:
  - name: my-registry-secret
```

### Application Configuration

The chart supports two methods for application configuration:

#### Method 1: ConfigMap-based (Recommended)

Enable ConfigMap-based configuration:

```yaml
config:
  enabled: true

applicationConfig: |
  spring:
    datasource:
      url: jdbc:h2:mem:moneta
      username: sa
      password: sa
    jpa:
      hibernate:
        ddl-auto: create-drop
  
  mill:
    services:
      grinder:
        enable: true
      jet-http:
        enable: true
```

This configuration is mounted at `/config/application.yml` and automatically loaded by Spring Boot.

#### Method 2: Pre-configured Images

If your image already contains configuration:

```yaml
config:
  enabled: false
```

### Secrets Management

**Important:** Never commit secrets to version control. Use one of these methods:

#### Method 1: Using values.yaml (Development Only)

```yaml
secrets:
  create: true
  data:
    SPRING_AI_OPENAI_API_KEY: "sk-your-key-here"
    SPRING_DATASOURCE_PASSWORD: "db-password"
```

#### Method 2: Using --set-string (Recommended for Production)

```bash
helm install mill-service ./mill-service \
  --set-string 'secrets.data.SPRING_AI_OPENAI_API_KEY=sk-...' \
  --set-string 'secrets.data.SPRING_DATASOURCE_PASSWORD=secret-password'
```

#### Method 3: Using External Secrets

```yaml
secrets:
  create: false
  existingSecretName: "my-existing-secret"
```

#### Method 4: Using Sealed Secrets or External Secrets Operator

For production, consider using:
- [Sealed Secrets](https://github.com/bitnami-labs/sealed-secrets)
- [External Secrets Operator](https://external-secrets.io/)

**How Secrets Work:**

1. Secrets defined in `secrets.data` are created as Kubernetes Secrets
2. They are automatically mounted as environment variables via `envFrom`
3. You can reference them in `args` using `${VAR_NAME}` syntax (see below)

### Command Line Arguments

You can pass command-line arguments to the application. Environment variables from secrets are automatically expanded:

```yaml
args:
  - "--spring.profiles.active=moneta"
  - "--spring.ai.model.chat=openai"
  - "--spring.ai.openai.api-key=${SPRING_AI_OPENAI_API_KEY}"
  - "--spring.ai.openai.chat.options.model=gpt-4.1"
  - "--spring.ai.model.embedding=openai"
```

**How it works:**
- Arguments are passed through a shell wrapper
- Environment variables (from secrets) are expanded before passing to the application
- Use `${VAR_NAME}` syntax to reference environment variables

**Example with secrets:**

```yaml
secrets:
  create: true
  data:
    SPRING_AI_OPENAI_API_KEY: "sk-..."

args:
  - "--spring.profiles.active=moneta"
  - "--spring.ai.openai.api-key=${SPRING_AI_OPENAI_API_KEY}"
```

### Additional Environment Variables

For non-sensitive configuration:

```yaml
env:
  - name: JAVA_OPTS
    value: "-Xmx1g -Xms512m"
  - name: SPRING_PROFILES_ACTIVE
    value: "prod"
```

### Resource Limits

Configure CPU and memory:

```yaml
resources:
  limits:
    cpu: 2000m
    memory: 2Gi
  requests:
    cpu: 500m
    memory: 512Mi
```

### Health Probes

Health probes are configured by default:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: http
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: http
  initialDelaySeconds: 10
  periodSeconds: 5
```

## Deployment

### Initial Deployment

```bash
helm install mill-service ./mill-service \
  --namespace default \
  --create-namespace
```

### Upgrade Deployment

```bash
helm upgrade mill-service ./mill-service \
  --namespace default
```

### Upgrade with Values

```bash
helm upgrade mill-service ./mill-service \
  --namespace default \
  --set replicaCount=3 \
  --set image.tag=v0.7.0
```

### Dry Run

Test changes before applying:

```bash
helm upgrade mill-service ./mill-service \
  --namespace default \
  --dry-run --debug
```

## Updating and Restarting

### Update Configuration Only (ConfigMap)

To update only the application configuration without a full redeploy:

```bash
# Method 1: Using Helm template
helm template mill-service ./mill-service \
  -s templates/configmap.yaml \
  | kubectl apply -f - -n default

# Restart pods to pick up new config
kubectl rollout restart deployment/mill-service -n default

# Wait for rollout
kubectl rollout status deployment/mill-service -n default
```

### Update Secrets Only

```bash
# Update secret
kubectl create secret generic mill-service \
  --from-literal=SPRING_AI_OPENAI_API_KEY=sk-new-key \
  --dry-run=client -o yaml \
  | kubectl apply -f - -n default

# Restart pods
kubectl rollout restart deployment/mill-service -n default
```

### Restart Pods

```bash
# Restart deployment
kubectl rollout restart deployment/mill-service -n default

# Check status
kubectl rollout status deployment/mill-service -n default

# View pods
kubectl get pods -n default -l app.kubernetes.io/name=mill-service
```

### Rollback

```bash
# List revisions
helm history mill-service -n default

# Rollback to previous version
helm rollback mill-service -n default

# Rollback to specific revision
helm rollback mill-service 3 -n default
```

## Accessing the Service

### Port Forwarding

**HTTP (REST API and Web UI):**
```bash
kubectl port-forward svc/mill-service 8080:8080 -n default
# Access at http://localhost:8080
```

**gRPC:**
```bash
kubectl port-forward svc/mill-service 9099:9099 -n default
# Connect to localhost:9099
```

### Service Endpoints

The service exposes:
- **HTTP**: Port 8080 (REST API, Web UI, Actuator endpoints)
- **gRPC**: Port 9099 (Mill protocol)

### Health Endpoints

- Liveness: `http://localhost:8080/actuator/health/liveness`
- Readiness: `http://localhost:8080/actuator/health/readiness`
- Health: `http://localhost:8080/actuator/health`

### Exposing Externally

To expose the service externally, change the service type:

```yaml
service:
  type: LoadBalancer
  # or
  type: NodePort
```

Or use an Ingress:

```yaml
# Add to values.yaml or use --set
ingress:
  enabled: true
  className: nginx
  hosts:
    - host: mill.example.com
      paths:
        - path: /
          pathType: Prefix
```

## Troubleshooting

### Check Pod Status

```bash
kubectl get pods -n default -l app.kubernetes.io/name=mill-service
```

### View Pod Logs

```bash
# All pods
kubectl logs -n default -l app.kubernetes.io/name=mill-service -f

# Specific pod
kubectl logs -n default mill-service-xxxxx-xxxxx -f
```

### Describe Pod

```bash
kubectl describe pod -n default mill-service-xxxxx-xxxxx
```

### Check ConfigMap

```bash
kubectl get configmap mill-service-config -n default -o yaml
```

### Check Secrets

```bash
# List secrets (values are base64 encoded)
kubectl get secret mill-service -n default -o yaml

# Decode a secret value
kubectl get secret mill-service -n default -o jsonpath='{.data.SPRING_AI_OPENAI_API_KEY}' | base64 -d
```

### Common Issues

**Issue: Pod in CrashLoopBackOff**

```bash
# Check logs
kubectl logs -n default mill-service-xxxxx-xxxxx --previous

# Common causes:
# - Missing or invalid secrets
# - Configuration errors
# - Image pull errors
```

**Issue: ImagePullBackOff**

```bash
# Check image name and pull policy
kubectl describe pod -n default mill-service-xxxxx-xxxxx | grep Image

# Verify image exists and is accessible
# Check imagePullSecrets if using private registry
```

**Issue: Configuration not applied**

```bash
# Verify ConfigMap is mounted
kubectl exec -n default mill-service-xxxxx-xxxxx -- ls -la /config

# Check if config.enabled is true
# Restart pods after ConfigMap update
```

**Issue: Secrets not working in args**

- Ensure secrets are defined in `secrets.data`
- Verify secret name matches: `${SECRET_NAME}`
- Check that `secrets.create: true`
- Restart pods after secret update

## Examples

### Example 1: Basic Deployment with Moneta Profile

```yaml
# values.yaml
image:
  repository: qpointz/mill-service-samples
  tag: "v0.6.0-dev"

args:
  - "--spring.profiles.active=moneta"

config:
  enabled: true

applicationConfig: |
  spring:
    datasource:
      url: jdbc:h2:mem:moneta
      username: sa
      password: sa
  mill:
    services:
      grinder:
        enable: true
      jet-http:
        enable: true
```

### Example 2: Production Deployment with Secrets

```bash
helm install mill-service ./mill-service \
  --namespace production \
  --create-namespace \
  --set replicaCount=3 \
  --set image.tag=v0.6.0 \
  --set-string 'secrets.data.SPRING_AI_OPENAI_API_KEY=sk-prod-key' \
  --set-string 'secrets.data.SPRING_DATASOURCE_PASSWORD=prod-db-password' \
  --set 'args[0]=--spring.profiles.active=production' \
  --set 'args[1]=--spring.ai.openai.api-key=${SPRING_AI_OPENAI_API_KEY}' \
  --set resources.limits.cpu=2000m \
  --set resources.limits.memory=2Gi \
  --set resources.requests.cpu=500m \
  --set resources.requests.memory=512Mi
```

### Example 3: Using External Secrets

```yaml
# values.yaml
secrets:
  create: false
  existingSecretName: "mill-service-secrets"

# Create secret separately
kubectl create secret generic mill-service-secrets \
  --from-literal=SPRING_AI_OPENAI_API_KEY=sk-... \
  --namespace production
```

### Example 4: Custom Configuration File

```yaml
# values.yaml
config:
  enabled: false  # Use pre-configured image

args:
  - "--spring.config.location=file:/app/config/application-prod.yml"
  - "--spring.profiles.active=production"
```

### Example 5: Development with Local Image

```bash
# Build and tag locally
docker build -t mill-service:local .

# Use local image
helm install mill-service ./mill-service \
  --set image.name=mill-service:local \
  --set image.pullPolicy=Never
```

## Chart Values Reference

Key configuration options:

| Parameter | Description | Default |
|-----------|-------------|---------|
| `replicaCount` | Number of replicas | `1` |
| `image.repository` | Image repository | `qpointz/mill-service-samples` |
| `image.tag` | Image tag | `v0.6.0-dev` |
| `image.name` | Full image name (overrides repository:tag) | `""` |
| `service.httpPort` | HTTP port | `8080` |
| `service.grpcPort` | gRPC port | `9099` |
| `config.enabled` | Enable ConfigMap-based config | `true` |
| `secrets.create` | Create Kubernetes Secret | `true` |
| `secrets.data` | Secret key-value pairs | `{}` |
| `args` | Command-line arguments | `[]` |
| `resources` | Resource limits/requests | `{}` |

See `values.yaml` for all available options.

## Support

For issues and questions:
- Check the [troubleshooting](#troubleshooting) section
- Review pod logs and events
- Verify configuration with `helm template --debug`

## License

See the main project license.
