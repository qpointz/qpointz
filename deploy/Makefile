.PHONY: help local-dev-start local-dev-start-ui local-dev-stop local-dev-reset local-dev-logs \
	azure-aca-deploy azure-aca-destroy gcp-cloudrun-deploy gcp-cloudrun-destroy gcp-cloudrun-sync

LOCAL_DEV_COMPOSE_FILE := local-dev/docker-compose.yml
LOCAL_DEV_DATA_DIR := ../.data/local-dev

help:
	@echo "Available targets:"
	@echo ""
	@echo "Local development (Docker Compose):"
	@echo "  make local-dev-start  # Start Postgres + ChromaDB"
	@echo "  make local-dev-start-ui # Start Postgres + ChromaDB + UIs"
	@echo "  make local-dev-stop   # Stop containers"
	@echo "  make local-dev-reset  # Stop + delete persisted data + start clean"
	@echo "  make local-dev-logs   # Tail logs (SERVICE=postgres|chromadb optional)"
	@echo ""
	@echo "UI ports (overridable):"
	@echo "  ADMINER_HTTP_PORT=$(ADMINER_HTTP_PORT:-48080) (Adminer)"
	@echo "  CHROMA_ADMIN_HTTP_PORT=$(CHROMA_ADMIN_HTTP_PORT:-43000) (ChromaDB Admin)"
	@echo ""
	@echo "Azure Container Apps (see azure-container-apps/README.md):"
	@echo "  make azure-aca-deploy   # build image, push ACR, deploy Container App"
	@echo "  make azure-aca-destroy  # delete resource group from azure-container-apps/.env"
	@echo ""
	@echo "Google Cloud Run (see gcp/cloud-run/README.md):"
	@echo "  make gcp-cloudrun-deploy   # tofu apply in gcp/cloud-run"
	@echo "  make gcp-cloudrun-sync     # upload Skymill files to GCS (sync.env)"
	@echo "  make gcp-cloudrun-destroy  # tofu destroy in gcp/cloud-run"

azure-aca-deploy:
	./azure-container-apps/deploy.sh

azure-aca-destroy:
	./azure-container-apps/destroy.sh

GCP_CLOUDRUN_DIR := gcp/cloud-run
TOFU ?= tofu

gcp-cloudrun-deploy:
	cd "$(GCP_CLOUDRUN_DIR)" && $(TOFU) init && $(TOFU) apply

gcp-cloudrun-destroy:
	cd "$(GCP_CLOUDRUN_DIR)" && $(TOFU) destroy

gcp-cloudrun-sync:
	cd "$(GCP_CLOUDRUN_DIR)" && ./sync-bucket.sh

local-dev-start:
	docker compose -f "$(LOCAL_DEV_COMPOSE_FILE)" up -d

local-dev-start-ui:
	docker compose -f "$(LOCAL_DEV_COMPOSE_FILE)" --profile ui up -d --build

local-dev-stop:
	docker compose -f "$(LOCAL_DEV_COMPOSE_FILE)" down

local-dev-reset:
	docker compose -f "$(LOCAL_DEV_COMPOSE_FILE)" down
	powershell -NoProfile -ExecutionPolicy Bypass -Command "if (Test-Path '$(LOCAL_DEV_DATA_DIR)/postgres') { Remove-Item -Recurse -Force '$(LOCAL_DEV_DATA_DIR)/postgres' } ; if (Test-Path '$(LOCAL_DEV_DATA_DIR)/chromadb') { Remove-Item -Recurse -Force '$(LOCAL_DEV_DATA_DIR)/chromadb' }"
	docker compose -f "$(LOCAL_DEV_COMPOSE_FILE)" up -d

local-dev-logs:
ifeq ($(strip $(SERVICE)),)
	docker compose -f "$(LOCAL_DEV_COMPOSE_FILE)" logs -f --tail=200
else
	docker compose -f "$(LOCAL_DEV_COMPOSE_FILE)" logs -f --tail=200 "$(SERVICE)"
endif
