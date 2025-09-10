# Mill Grinder UI Makefile
# Common commands for development and build

.PHONY: help install dev build preview lint clean test generate-api

# Default target
help:
	@echo "Available commands:"
	@echo "  install      - Install dependencies"
	@echo "  dev          - Start development server"
	@echo "  build        - Build for production"
	@echo "  preview      - Preview production build"
	@echo "  lint         - Run linter"
	@echo "  lint-fix     - Fix linting issues automatically"
	@echo "  clean        - Clean build artifacts"
	@echo "  test         - Run tests"
	@echo "  type-check   - Run TypeScript type checking"
	@echo "  format       - Format code with prettier"
	@echo "  deps-check   - Check for outdated dependencies"
	@echo "  generate-api - Generate TypeScript API from OpenAPI JSON"
	@echo "  api-clean    - Clean generated API files"

# Install dependencies
install:
	npm install

# Start development server
dev:
	npm run dev

# Build for production
build:
	npm run build

# Preview production build
preview:
	npm run preview

# Run linter
lint:
	npm run lint

# Fix linting issues
lint-fix:
	npm run lint -- --fix

# Clean build artifacts
clean:
	rm -rf dist
	rm -rf .vite-inspect
	rm -rf node_modules/.tmp
	rm -rf ../mill-grinder-service/src/main/resources/static/app

# Run tests (if available)
test:
	npm test

# Type checking
type-check:
	npx tsc --noEmit

# Format code (if prettier is configured)
format:
	npx prettier --write "src/**/*.{ts,tsx,js,jsx,json,css,md}"

# Check for outdated dependencies
deps-check:
	npm outdated

# Full clean and rebuild
rebuild: clean install build

# Development workflow
dev-setup: install
	@echo "Development environment ready!"
	@echo "Run 'make dev' to start the development server"

# Generate TypeScript API from OpenAPI JSON
generate-api:
	npx @openapitools/openapi-generator-cli generate \
		-i openapi.json \
		-g typescript-axios \
		-o src/api/mill \
		--additional-properties=typescriptThreePlus=true,withInterfaces=true,withNodeImports=false
	@sed -i 's|export const BASE_PATH = "http://localhost".replace(/\\/+$$/, "");|export const BASE_PATH = "".replace(/\\/+$$/, "");|g' src/api/mill/base.ts
	@rm -f src/api/mill/git_push.sh
	@echo "API generated successfully in src/api/mill/"


# Clean generated API files
api-clean:
	rm -rf src/api/mill
	@echo "Generated API files cleaned"

# Production deployment
deploy: clean install build
	@echo "Production build complete!"
	@echo "Files are in ../mill-grinder-service/src/main/resources/static/app"