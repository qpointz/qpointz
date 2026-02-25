SHELL := /bin/bash

.PHONY: help helm-samples-moneta helm-istio port-forward port-forward-istio port-forward-direct k8s-samples-delete

KUBE_NS ?= "mill-svc"
KUBE_NS_SAMPLES = $(KUBE_NS)-samples
KUBECTL ?= kubectl

help:
	@echo "Available targets:"
	@echo ""
	@echo "Helm:"
	@echo "  make helm-samples-moneta  # Deploy moneta sample using helm chart"
	@echo "  make helm-istio           # Deploy Istio traffic management"
	@echo ""
	@echo "Access (run in separate terminal, use KUBECTL=microk8s.kubectl for MicroK8s):"
	@echo "  make port-forward         # Via Istio ingress (HTTP 8080)"
	@echo "  make port-forward-direct  # Direct to mill-service (bypass Istio)"

helm-samples-moneta:
	@echo "Deploying moneta sample:"
	$(if $(OPENAI_API_KEY),, $(error OPENAI_API_KEY is not set))
	$(KUBECTL) create namespace $(KUBE_NS_SAMPLES) --dry-run=client -o yaml | $(KUBECTL) apply -f - && \
	helm upgrade mill-samples-moneta ./helm/mill-service --namespace $(KUBE_NS_SAMPLES) \
		--install \
		--set config.enable=false \
	  	--set 'args[0]=--spring.ai.model.chat=openai' \
	  	--set 'args[1]=--spring.ai.openai.chat.options.model=gpt-4.1' \
	  	--set 'args[2]=--spring.ai.model.embedding=openai' \
		--set 'args[3]=--spring.profiles.active=moneta' \
		--set secrets.data.SPRING_AI_OPENAI_API_KEY=$(OPENAI_API_KEY)

helm-istio:
	@echo "Deploying mill service istio:"
	$(KUBECTL) create namespace $(KUBE_NS_SAMPLES) --dry-run=client -o yaml | $(KUBECTL) apply -f - && \
	helm upgrade mill-samples-istio ./helm/mill-service-istio --namespace $(KUBE_NS_SAMPLES) \
		--install \
		--set service.name=mill-samples-moneta-mill-service \
		--set service.namespace=$(KUBE_NS_SAMPLES)

# Port-forward via Istio ingress gateway (HTTP only; gRPC via port-forward-direct)
port-forward:
	@echo "Port-forwarding Istio ingress: http://localhost:8080 (HTTP)"
	@echo "For gRPC use: make port-forward-direct"
	@echo "Press Ctrl+C to stop"
	$(KUBECTL) port-forward svc/istio-ingressgateway 8080:80 -n istio-system

# Port-forward direct to mill-service (bypass Istio, works without helm-istio)
port-forward-direct:
	@echo "Port-forwarding mill-service: http://localhost:8080 (HTTP), localhost:9090 (gRPC)"
	@echo "Press Ctrl+C to stop"
	$(KUBECTL) port-forward svc/mill-samples-moneta-mill-service 8080:8080 9090:9090 -n $(KUBE_NS_SAMPLES)

k8s-samples-delete:
	@echo "Deleting namespace $(KUBE_NS_SAMPLES):"
	$(KUBECTL) delete namespace $(KUBE_NS_SAMPLES)







