SHELL := /bin/bash

.PHONY: help build test clean ai-test svc-build \
	tools-build-info tools-build-sem-release \
	tools-build-minica tools-build-deploy-tools tools-build-all \
	git-clean-branches

help:
	@echo "Available targets:"
	@echo ""
	@echo "Core:"
	@echo "  make build           # Compile sources and run unit tests"
	@echo "  make test            # Execute the test suite"
	@echo "  make clean           # Remove build outputs"
	@echo "  make ai-test         # Run AI module tests"
	@echo "  make svc-build       # Build mill-service distribution and image"
	@echo ""
	@echo "UI (see ui/Makefile for all targets):"
	@echo "  make -C ui help      # Show all UI targets (V1 + V2)"
	@echo ""
	@echo "Git:"
	@echo "  make git-clean-branches  # Delete local feat/ poc/ fix/ branches with no remote"
	@echo ""
	@echo "Tools (CI/CD build images):"
	@echo "  make tools-build-info        # Display build tools version and registry"
	@echo "  make tools-build-sem-release # Build and push semantic-release image"
	@echo "  make tools-build-minica      # Build and push minica image"
	@echo "  make tools-build-deploy-tools # Build and push deploy-tools image"
	@echo "  make tools-build-all         # Build and push all tool images"

build:
	./gradlew build

test:
	./gradlew test

clean:
	./gradlew clean

ai-test:
	cd ai && ./gradlew test

svc-build:
	cd apps && ./gradlew clean installBootDist && \
		docker buildx build -f mill-service/src/main/docker/Dockerfile -t mill-service ./mill-service

BUILD_TOOLS_VERSION := $(shell grep "BUILD_TOOLS_VERSION:" .gitlab/vars.yml | awk '{print $$2}')
BUILD_TOOLS_REGISTRY := $(shell grep "BUILD_TOOLS_REGISTRY:" .gitlab/vars.yml | awk '{print $$2}')

tools-build-info:
	@echo "Version: $(BUILD_TOOLS_VERSION)" 
	@echo "Registry: $(BUILD_TOOLS_REGISTRY)"
	
tools-build-sem-release:
	cd .gitlab/docker/semantic-release && \
	docker build -t $(BUILD_TOOLS_REGISTRY)/semantic-release:$(BUILD_TOOLS_VERSION) . && \
	docker push $(BUILD_TOOLS_REGISTRY)/semantic-release:$(BUILD_TOOLS_VERSION)

tools-build-minica:
	cd .gitlab/docker/minica && \
	docker build -t $(BUILD_TOOLS_REGISTRY)/minica:$(BUILD_TOOLS_VERSION) . && \
	docker push $(BUILD_TOOLS_REGISTRY)/minica:$(BUILD_TOOLS_VERSION)

tools-build-deploy-tools:
	cd .gitlab/docker/deploy-tools && \
	docker build -t $(BUILD_TOOLS_REGISTRY)/deploy-tools:$(BUILD_TOOLS_VERSION) . && \
	docker push $(BUILD_TOOLS_REGISTRY)/deploy-tools:$(BUILD_TOOLS_VERSION)

tools-build-all: tools-build-sem-release tools-build-minica tools-build-deploy-tools

git-clean-branches:
	@git fetch origin --prune
	@for branch in $$(git for-each-ref --format='%(refname:short)' refs/heads/build refs/heads/feat/ refs/heads/poc/ refs/heads/fix/); do \
		if ! git show-ref --quiet refs/remotes/origin/$$branch; then \
			echo "Deleting $$branch (no remote on origin)"; \
			git branch -D $$branch; \
		else \
			echo "Keeping $$branch (remote exists)"; \
		fi; \
	done
