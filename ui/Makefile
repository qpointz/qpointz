SHELL := /bin/bash

V1_DIR := mill-grinder-ui
V2_DIR := mill-ui

.PHONY: help build install clean test test-ci lint \
	v1-install v1-dev v1-build v1-preview v1-lint v1-lint-fix v1-clean \
	v1-test v1-test-ci v1-type-check v1-format v1-deps-check v1-rebuild \
	v1-dev-setup v1-generate-api v1-api-clean v1-deploy \
	v2-install v2-dev v2-build v2-preview v2-lint v2-lint-fix v2-clean \
	v2-test v2-test-ci v2-test-coverage v2-type-check v2-deps-check \
	v2-rebuild v2-dev-setup

help:
	@echo "UI build targets"
	@echo ""
	@echo "Both:"
	@echo "  make build           # Build V1 and V2"
	@echo "  make install         # Install dependencies for V1 and V2"
	@echo "  make clean           # Clean V1 and V2"
	@echo "  make test-ci         # Run tests for V1 and V2 (CI/headless)"
	@echo "  make lint            # Lint V1 and V2"
	@echo ""
	@echo "V1 — $(V1_DIR):"
	@echo "  make v1-install       # Install npm dependencies"
	@echo "  make v1-dev           # Start the development server"
	@echo "  make v1-build         # Build the production bundle"
	@echo "  make v1-preview       # Preview the production build"
	@echo "  make v1-lint          # Run lint checks"
	@echo "  make v1-lint-fix      # Autofix lint issues"
	@echo "  make v1-clean         # Remove build and temp artifacts"
	@echo "  make v1-test          # Run tests (watch mode)"
	@echo "  make v1-test-ci       # Run tests once (CI/headless)"
	@echo "  make v1-type-check    # Run TypeScript type checking"
	@echo "  make v1-format        # Format sources with Prettier"
	@echo "  make v1-deps-check    # Check for outdated npm dependencies"
	@echo "  make v1-rebuild       # Clean, install, and rebuild"
	@echo "  make v1-dev-setup     # Prepare development environment"
	@echo "  make v1-generate-api  # Generate API client from OpenAPI spec"
	@echo "  make v1-api-clean     # Remove generated API client files"
	@echo "  make v1-deploy        # Build artifacts for service packaging"
	@echo ""
	@echo "V2 — $(V2_DIR):"
	@echo "  make v2-install       # Install npm dependencies"
	@echo "  make v2-dev           # Start the development server"
	@echo "  make v2-build         # Build the production bundle"
	@echo "  make v2-preview       # Preview the production build"
	@echo "  make v2-lint          # Run lint checks"
	@echo "  make v2-lint-fix      # Autofix lint issues"
	@echo "  make v2-clean         # Remove build and temp artifacts"
	@echo "  make v2-test          # Run tests (watch mode)"
	@echo "  make v2-test-ci       # Run tests once (CI/headless)"
	@echo "  make v2-test-coverage # Run tests with coverage"
	@echo "  make v2-type-check    # Run TypeScript type checking"
	@echo "  make v2-deps-check    # Check for outdated npm dependencies"
	@echo "  make v2-rebuild       # Clean, install, and rebuild"
	@echo "  make v2-dev-setup     # Prepare development environment"

# ──────────────────────────────────────────────
# Both
# ──────────────────────────────────────────────

build: v1-build v2-build

install: v1-install v2-install

clean: v1-clean v2-clean

test-ci: v1-test-ci v2-test-ci

lint: v1-lint v2-lint

# ──────────────────────────────────────────────
# V1 — mill-grinder-ui
# ──────────────────────────────────────────────

v1-install:
	cd $(V1_DIR) && npm install

v1-dev:
	cd $(V1_DIR) && npm run dev

v1-build:
	cd $(V1_DIR) && npm run build

v1-preview:
	cd $(V1_DIR) && npm run preview

v1-lint:
	cd $(V1_DIR) && npm run lint

v1-lint-fix:
	cd $(V1_DIR) && npm run lint -- --fix

v1-clean:
	rm -rf $(V1_DIR)/dist
	rm -rf $(V1_DIR)/.vite-inspect
	rm -rf $(V1_DIR)/node_modules/.tmp
	rm -rf mill-grinder-service/src/main/resources/static/app

v1-test:
	cd $(V1_DIR) && npm test

v1-test-ci:
	cd $(V1_DIR) && npm run test:run

v1-type-check:
	cd $(V1_DIR) && npx tsc --noEmit

v1-format:
	cd $(V1_DIR) && npx prettier --write "src/**/*.{ts,tsx,js,jsx,json,css,md}"

v1-deps-check:
	cd $(V1_DIR) && npm outdated

v1-rebuild: v1-clean v1-install v1-build

v1-dev-setup: v1-install
	@echo "V1 development environment ready!"
	@echo "Run 'make v1-dev' to start the development server"

v1-generate-api:
	cd $(V1_DIR) && npx @openapitools/openapi-generator-cli generate \
		-i openapi.json \
		-g typescript-axios \
		-o src/api/mill \
		--additional-properties=typescriptThreePlus=true,withInterfaces=true,withNodeImports=false
	cd $(V1_DIR) && sed -i 's|export const BASE_PATH = "http://localhost".replace(/\\/+$$/, "");|export const BASE_PATH = "".replace(/\\/+$$/, "");|g' src/api/mill/base.ts
	cd $(V1_DIR) && rm -f src/api/mill/git_push.sh
	@cd $(V1_DIR) && echo "API generated successfully in src/api/mill/"

v1-api-clean:
	rm -rf $(V1_DIR)/src/api/mill
	@echo "Generated API files cleaned"

v1-deploy: v1-clean v1-install v1-build
	@echo "V1 production build complete!"
	@echo "Files are in mill-grinder-service/src/main/resources/static/app"

# ──────────────────────────────────────────────
# V2 — mill-ui
# ──────────────────────────────────────────────

v2-install:
	cd $(V2_DIR) && npm install

v2-dev:
	cd $(V2_DIR) && npm run dev

v2-build:
	cd $(V2_DIR) && npm run build

v2-preview:
	cd $(V2_DIR) && npm run preview

v2-lint:
	cd $(V2_DIR) && npm run lint

v2-lint-fix:
	cd $(V2_DIR) && npm run lint -- --fix

v2-clean:
	rm -rf $(V2_DIR)/dist
	rm -rf $(V2_DIR)/node_modules/.tmp
	rm -rf mill-grinder-service/src/main/resources/static/app/v2

v2-test:
	cd $(V2_DIR) && npm test

v2-test-ci:
	cd $(V2_DIR) && npm run test:ci

v2-test-coverage:
	cd $(V2_DIR) && npm run test:coverage

v2-type-check:
	cd $(V2_DIR) && npx tsc --noEmit

v2-deps-check:
	cd $(V2_DIR) && npm outdated

v2-rebuild: v2-clean v2-install v2-build

v2-dev-setup: v2-install
	@echo "V2 development environment ready!"
	@echo "Run 'make v2-dev' to start the development server"
