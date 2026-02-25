SHELL := /bin/bash

DESKTOP_DIR := mill-desktop

.PHONY: help build-docker \
	desktop-install desktop-build desktop-launch desktop-clean

help:
	@echo "Available targets:"
	@echo ""
	@echo "make build-docker      # rebuild docker images"
	@echo "make run-moneta        # runs moneta sample docker"
	@echo ""
	@echo "Desktop — $(DESKTOP_DIR):"
	@echo "  make desktop-install  # Install npm dependencies"
	@echo "  make desktop-build    # Build mill-desktop"
	@echo "  make desktop-launch   # Launch mill-desktop in dev mode"
	@echo "  make desktop-clean    # Remove desktop build artifacts"

rebuild:
	cd ../services/mill-grinder-ui ; \
	npm install ; \
	npm run build
	./gradlew clean installBootDist assembleSamples

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