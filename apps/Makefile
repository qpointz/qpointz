SHELL := /bin/bash

DESKTOP_DIR := mill-desktop

.PHONY: help build-docker run-moneta run-mill-postgres postgres-up postgres-down postgres-logs \
	desktop-install desktop-build desktop-launch desktop-clean

help:
	@echo "Available targets:"
	@echo ""
	@echo "make build-docker      # rebuild docker images"
	@echo "make run-moneta        # runs moneta sample docker"
	@echo "make run-mill-postgres # run mill-service with postgres profile"
	@echo "make postgres-up       # start local postgres docker"
	@echo "make postgres-down     # stop and remove local postgres docker"
	@echo "make postgres-logs     # tail postgres container logs"
	@echo ""
	@echo "Desktop — $(DESKTOP_DIR):"
	@echo "  make desktop-install  # Install npm dependencies"
	@echo "  make desktop-build    # Build mill-desktop"
	@echo "  make desktop-launch   # Launch mill-desktop in dev mode"
	@echo "  make desktop-clean    # Remove desktop build artifacts"

rebuild:
	cd ../ui/mill-grinder-ui ; \
	npm install ; \
	npm run build
	cd ../ ; \
	./gradlew clean installBootDist

build-docker: rebuild
	docker buildx build --tag mill-service --progress plain --file mill-service/src/main/docker/base/Dockerfile ./mill-service
	docker buildx build --tag mill-service-samples --progress plain \
						--file mill-service/src/main/docker/samples/Dockerfile \
						--build-arg  BASE_IMAGE=mill-service \
						./mill-service

run-moneta : build-docker
	docker run -ti -p "8080:8080" -p "9090:9090" \
		mill-service-samples - \
		--logging.level.root=INFO \
		--spring.profiles.active=moneta \
        --spring.ai.model.chat=openai \
        --spring.ai.model.embedding=openai \
        --spring.ai.openai.api-key=$(OPENAI_API_KEY) \
        --spring.ai.openai.chat.options.temperature=0.5 \
        --spring.ai.openai.chat.options.model=gpt-4

run-mill-postgres:
	cd ../ && \
	MILL_DB_URL=$${MILL_DB_URL:-jdbc:postgresql://localhost:5432/mill} \
	MILL_DB_USERNAME=$${MILL_DB_USERNAME:-postgres} \
	MILL_DB_PASSWORD=$${MILL_DB_PASSWORD:-postgres} \
	./gradlew :apps:mill-service:bootRun --args='--spring.profiles.active=skymill,postgres'

postgres-up:
	docker run --name mill-postgres \
		-e POSTGRES_DB=$${MILL_DB_NAME:-mill} \
		-e POSTGRES_USER=$${MILL_DB_USERNAME:-postgres} \
		-e POSTGRES_PASSWORD=$${MILL_DB_PASSWORD:-postgres} \
		-p $${MILL_DB_PORT:-5432}:5432 \
		-d postgres:latest

postgres-down:
	docker rm -f mill-postgres

postgres-logs:
	docker logs -f mill-postgres

info:
	@echo $(OPENAI_API_KEY)

# ──────────────────────────────────────────────
# Desktop — mill-desktop
# ──────────────────────────────────────────────

desktop-install:
	cd $(DESKTOP_DIR) && npm install

desktop-build:
	cd $(DESKTOP_DIR) && npm run build

desktop-launch:
	cd $(DESKTOP_DIR) && npm run dev

desktop-clean:
	rm -rf $(DESKTOP_DIR)/dist
	rm -rf $(DESKTOP_DIR)/dist-electron
	rm -rf $(DESKTOP_DIR)/release