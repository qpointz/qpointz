SHELL := /bin/bash

.PHONY: help local-dev-start local-dev-start-ui local-dev-stop local-dev-reset local-dev-logs \
	azure-aca-deploy azure-aca-destroy azure-minimal-plan azure-minimal-apply azure-minimal-destroy \
	gcp-cloudrun-deploy gcp-cloudrun-destroy gcp-cloudrun-sync \
	docker-minimal-run

LOCAL_DEV_COMPOSE_FILE := local-dev/docker-compose.yml
LOCAL_DEV_DATA_DIR := ../.data/local-dev
TOFU ?= tofu

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
	@echo "Azure minimal blueprint (see azure/minimal-blueprint/README.md):"
	@echo "  make azure-minimal-plan     # tofu plan"
	@echo "  make azure-minimal-apply    # tofu apply"
	@echo "  make azure-minimal-destroy  # tofu destroy"
	@echo ""
	@echo "Azure Container Apps legacy (see azure-container-apps/README.md):"
	@echo "  make azure-aca-deploy   # build image, push ACR, deploy Container App"
	@echo "  make azure-aca-destroy  # delete resource group from azure-container-apps/.env"
	@echo ""
	@echo "Google Cloud Run (see gcp/cloud-run/README.md):"
	@echo "  make gcp-cloudrun-deploy   # tofu apply in gcp/cloud-run"
	@echo "  make gcp-cloudrun-sync     # upload Skymill files to GCS (sync.env)"
	@echo "  make gcp-cloudrun-destroy  # tofu destroy in gcp/cloud-run"
	@echo ""
	@echo "Docker minimal blueprint (see docker/minimal-blueprint/README.md):"
	@echo "  make docker-minimal-run      # ./run.sh"

azure-aca-deploy:
	./azure-container-apps/deploy.sh

azure-aca-destroy:
	./azure-container-apps/destroy.sh

AZURE_MINIMAL_DIR := azure/minimal-blueprint

azure-minimal-plan:
	cd "$(AZURE_MINIMAL_DIR)" && $(TOFU) init && $(TOFU) plan -var-file=terraform.tfvars

azure-minimal-apply:
	cd "$(AZURE_MINIMAL_DIR)" && $(TOFU) init && $(TOFU) apply -var-file=terraform.tfvars

azure-minimal-destroy:
	cd "$(AZURE_MINIMAL_DIR)" && $(TOFU) destroy -var-file=terraform.tfvars

GCP_CLOUDRUN_DIR := gcp/cloud-run

gcp-cloudrun-deploy:
	cd "$(GCP_CLOUDRUN_DIR)" && $(TOFU) init && $(TOFU) apply

gcp-cloudrun-destroy:
	cd "$(GCP_CLOUDRUN_DIR)" && $(TOFU) destroy

gcp-cloudrun-sync:
	cd "$(GCP_CLOUDRUN_DIR)" && ./sync-bucket.sh

DOCKER_MINIMAL_DIR := docker/minimal-blueprint

docker-minimal-run:
	cd "$(DOCKER_MINIMAL_DIR)" && ./run.sh

local-dev-start:
	docker compose -f "$(LOCAL_DEV_COMPOSE_FILE)" up -d

local-dev-start-ui:
	docker compose -f "$(LOCAL_DEV_COMPOSE_FILE)" --profile ui up -d --build

local-dev-stop:
	docker compose -f "$(LOCAL_DEV_COMPOSE_FILE)" down

local-dev-reset:
	docker compose -f "$(LOCAL_DEV_COMPOSE_FILE)" down
	rm -rf "$(LOCAL_DEV_DATA_DIR)/postgres" "$(LOCAL_DEV_DATA_DIR)/chromadb" "$(LOCAL_DEV_DATA_DIR)/authentik"
	docker compose -f "$(LOCAL_DEV_COMPOSE_FILE)" up -d

local-dev-logs:
ifeq ($(strip $(SERVICE)),)
	docker compose -f "$(LOCAL_DEV_COMPOSE_FILE)" logs -f --tail=200
else
	docker compose -f "$(LOCAL_DEV_COMPOSE_FILE)" logs -f --tail=200 "$(SERVICE)"
endif
