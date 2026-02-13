SHELL := /bin/bash

.PHONY: help build test clean ai-test svc-build \
	ui-install ui-dev ui-build ui-preview ui-lint ui-lint-fix ui-clean ui-test \
	ui-type-check ui-format ui-deps-check ui-rebuild ui-dev-setup ui-generate-api \
	ui-api-clean ui-deploy tools-build-info tools-build-sem-release \
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
	@echo "UI (mill-grinder):"
	@echo "  make ui-install      # Install npm dependencies"
	@echo "  make ui-dev          # Start the development server"
	@echo "  make ui-build        # Build the production bundle"
	@echo "  make ui-preview      # Preview the production build"
	@echo "  make ui-lint         # Run lint checks"
	@echo "  make ui-lint-fix     # Autofix lint issues"
	@echo "  make ui-clean        # Remove build and temp artifacts"
	@echo "  make ui-test         # Run UI tests"
	@echo "  make ui-type-check   # Run TypeScript type checking"
	@echo "  make ui-format       # Format UI sources with Prettier"
	@echo "  make ui-deps-check   # Check for outdated npm dependencies"
	@echo "  make ui-rebuild      # Clean, install, and rebuild the UI"
	@echo "  make ui-dev-setup    # Prepare development environment"
	@echo "  make ui-generate-api # Generate API client from OpenAPI spec"
	@echo "  make ui-api-clean    # Remove generated API client files"
	@echo "  make ui-deploy       # Build UI artifacts for service packaging"
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

ui-install:
	cd services/mill-grinder-ui && npm install

ui-dev:
	cd services/mill-grinder-ui && npm run dev

ui-build:
	cd services/mill-grinder-ui && npm run build

ui-preview:
	cd services/mill-grinder-ui && npm run preview

ui-lint:
	cd services/mill-grinder-ui && npm run lint

ui-lint-fix:
	cd services/mill-grinder-ui && npm run lint -- --fix

ui-clean:
	rm -rf services/mill-grinder-ui/dist
	rm -rf services/mill-grinder-ui/.vite-inspect
	rm -rf services/mill-grinder-ui/node_modules/.tmp
	rm -rf services/mill-grinder-service/src/main/resources/static/app

ui-test:
	cd services/mill-grinder-ui && npm test

ui-type-check:
	cd services/mill-grinder-ui && npx tsc --noEmit

ui-format:
	cd services/mill-grinder-ui && npx prettier --write "src/**/*.{ts,tsx,js,jsx,json,css,md}"

ui-deps-check:
	cd services/mill-grinder-ui && npm outdated

ui-rebuild: ui-clean ui-install ui-build

ui-dev-setup: ui-install
	@echo "Development environment ready!"
	@echo "Run 'make ui-dev' to start the development server"

ui-generate-api:
	cd services/mill-grinder-ui && npx @openapitools/openapi-generator-cli generate \
		-i openapi.json \
		-g typescript-axios \
		-o src/api/mill \
		--additional-properties=typescriptThreePlus=true,withInterfaces=true,withNodeImports=false
	cd services/mill-grinder-ui && sed -i 's|export const BASE_PATH = "http://localhost".replace(/\\/+$$/, "");|export const BASE_PATH = "".replace(/\\/+$$/, "");|g' src/api/mill/base.ts
	cd services/mill-grinder-ui && rm -f src/api/mill/git_push.sh
	@cd services/mill-grinder-ui && echo "API generated successfully in src/api/mill/"

ui-api-clean:
	rm -rf services/mill-grinder-ui/src/api/mill
	@echo "Generated API files cleaned"

ui-deploy: ui-clean ui-install ui-build
	@echo "Production build complete!"
	@echo "Files are in services/mill-grinder-service/src/main/resources/static/app"

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
