SHELL := /bin/bash

.PHONY: help build test clean ai-test svc-build \
	maven-local-publish \
	docs-build docs-serve \
	tools-build-info tools-build-sem-release \
	tools-build-minica tools-build-deploy-tools tools-build-all \
	git-clean-branches check-tools

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
	@echo "Publishing:"
	@echo "  make maven-local-publish  # Build Maven artifacts locally (unsigned)"
	@echo ""
	@echo "Docs:"
	@echo "  make docs-build           # Build full docs site (mkdocs + API docs)"
	@echo "  make docs-serve           # Serve docs site locally for preview"
	@echo ""
	@echo "UI (see ui/Makefile for all targets):"
	@echo "  make -C ui help      # Show all UI targets (V1 + V2)"
	@echo ""
	@echo "Environment:"
	@echo "  make check-tools         # Verify required/optional dev tools are installed"
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

maven-local-publish:
	./gradlew clean publish publishSonatypeBundle

docs-build:
	./gradlew dokkaGenerate
	cd docs/public && python -m mkdocs build
	cp -r build/dokka/html docs/public/site/api/kotlin

docs-serve: docs-build
	cd docs/public && python -m mkdocs serve

BUILD_TOOLS_VERSION := $(shell grep "BUILD_TOOLS_VERSION:" .gitlab/vars.yml 2>/dev/null | awk '{print $$2}')
BUILD_TOOLS_REGISTRY := $(shell grep "BUILD_TOOLS_REGISTRY:" .gitlab/vars.yml 2>/dev/null | awk '{print $$2}')

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

# ---------------------------------------------------------------------------
# check-tools — verify required and optional development tools
# ---------------------------------------------------------------------------
# To add a new tool, append a line to the _check_tool calls below.
# Signature:  _check_tool <command> <required|optional> <description>
# Version is obtained automatically via `<command> --version` (first line).

# Colours (only when stdout is a terminal)
_GREEN  := $(shell tput setaf 2 2>/dev/null || true)
_RED    := $(shell tput setaf 1 2>/dev/null || true)
_YELLOW := $(shell tput setaf 3 2>/dev/null || true)
_RESET  := $(shell tput sgr0 2>/dev/null || true)

# _check_tool <command> <required|optional> <description> [version_cmd]
# If version_cmd (4th arg) is empty, defaults to `<command> --version | head -1`.
define _check_tool
	@cmd="$(1)"; level="$(2)"; desc="$(3)"; ver_cmd="$(4)"; \
	if [ -z "$$ver_cmd" ]; then ver_cmd="$(1) --version 2>&1 | head -1"; fi; \
	if command -v "$$cmd" >/dev/null 2>&1; then \
		ver=$$(eval "$$ver_cmd" 2>/dev/null || echo "installed"); \
		printf "$(_GREEN)  ✓ %-18s$(_RESET) %s\n" "$$cmd" "$$ver"; \
	elif [ "$$level" = "required" ]; then \
		printf "$(_RED)  ✗ %-18s MISSING (required) — %s$(_RESET)\n" "$$cmd" "$$desc"; \
		touch /tmp/_check_tools_fail; \
	else \
		printf "$(_YELLOW)  - %-18s not found (optional) — %s$(_RESET)\n" "$$cmd" "$$desc"; \
	fi
endef

check-tools:
	@rm -f /tmp/_check_tools_fail
	@echo "Checking development tools..."
	@echo "=============================="
	$(call _check_tool,java,required,JDK 21+ — builds Gradle modules services/core/data)
	$(call _check_tool,python3,required,Python 3.10+ — mill-py client development)
	$(call _check_tool,git,required,version control)
	$(call _check_tool,protoc,required,Protocol Buffers compiler — generates proto stubs)
	$(call _check_tool,poetry,required,Python dependency management and packaging for mill-py)
	@# grpcio-tools is a Python package, not a standalone binary — check via import
	@if python3 -c "from grpc_tools import protoc" >/dev/null 2>&1; then \
		ver=$$(pip show grpcio-tools 2>/dev/null | grep Version | awk '{print $$2}'); \
		printf "$(_GREEN)  ✓ %-18s$(_RESET) %s\n" "grpcio-tools" "$${ver:-installed}"; \
	else \
		printf "$(_RED)  ✗ %-18s MISSING (required) — pip install grpcio-tools — proto codegen for mill-py$(_RESET)\n" "grpcio-tools"; \
		touch /tmp/_check_tools_fail; \
	fi
	$(call _check_tool,node,required,Node.js — UI builds mill-grinder-ui)
	$(call _check_tool,npm,required,npm — UI dependency management)
	$(call _check_tool,npx,required,npx — runs Node.js package binaries)
	$(call _check_tool,docker,optional,container builds svc-build / tools-build-*)
	$(call _check_tool,helm,optional,Kubernetes Helm chart packaging and deployment,helm version --short 2>&1 | head -1)
	$(call _check_tool,kubectl,optional,Kubernetes cluster management,kubectl version --client -o yaml 2>&1 | grep gitVersion | head -1)
	$(call _check_tool,terraform,optional,infrastructure provisioning)
	@echo ""
	@if [ -f /tmp/_check_tools_fail ]; then \
		rm -f /tmp/_check_tools_fail; \
		echo "$(_RED)Some required tools are missing. Install them before proceeding.$(_RESET)"; \
		exit 1; \
	else \
		echo "$(_GREEN)All required tools are available.$(_RESET)"; \
	fi
