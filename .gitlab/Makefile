SHELL := /bin/bash

BUILD_TOOLS_VERSION := $(shell grep "BUILD_TOOLS_VERSION:" $(CURDIR)/.vars.yml 2>/dev/null | awk '{print $$2}')
BUILD_TOOLS_REGISTRY := $(shell grep "BUILD_TOOLS_REGISTRY:" $(CURDIR)/.vars.yml 2>/dev/null | awk '{print $$2}')

.PHONY: help build-info build-sem-release build-minica build-deploy-tools build-all

help:
	@echo "Tools (CI/CD build images):"
	@echo "  make build-info         # Display build tools version and registry"
	@echo "  make build-sem-release  # Build and push semantic-release image"
	@echo "  make build-minica       # Build and push minica image"
	@echo "  make build-deploy-tools # Build and push deploy-tools image"
	@echo "  make build-all          # Build and push all tool images"

build-info:
	@echo "Version: $(BUILD_TOOLS_VERSION)"
	@echo "Registry: $(BUILD_TOOLS_REGISTRY)"

build-sem-release:
	cd docker/semantic-release && \
	docker build -t $(BUILD_TOOLS_REGISTRY)/semantic-release:$(BUILD_TOOLS_VERSION) . && \
	docker push $(BUILD_TOOLS_REGISTRY)/semantic-release:$(BUILD_TOOLS_VERSION)

build-minica:
	cd docker/minica && \
	docker build -t $(BUILD_TOOLS_REGISTRY)/minica:$(BUILD_TOOLS_VERSION) . && \
	docker push $(BUILD_TOOLS_REGISTRY)/minica:$(BUILD_TOOLS_VERSION)

build-deploy-tools:
	cd docker/deploy-tools && \
	docker build -t $(BUILD_TOOLS_REGISTRY)/deploy-tools:$(BUILD_TOOLS_VERSION) . && \
	docker push $(BUILD_TOOLS_REGISTRY)/deploy-tools:$(BUILD_TOOLS_VERSION)

build-all: build-sem-release build-minica build-deploy-tools
