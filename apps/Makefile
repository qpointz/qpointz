SHELL := /bin/bash

.PHONY: help build-docker

help:
	@echo "Available targets:"
	@echo ""
	@echo "make build-docker     # rebuild docker images"
	@echo "make run-moneta     # runs moneta sample docker"

rebuild:
	./gradlew clean installBootDist assembleSamples

build-docker: rebuild
	docker buildx build --tag mill-service --progress plain --file mill-service/src/main/docker/base/Dockerfile ./mill-service
	docker buildx build --tag mill-service-samples --progress plain \
						--file mill-service/src/main/docker/samples/Dockerfile \
						--build-arg  BASE_IMAGE=mill-service \
						./mill-service

run-moneta : build-docker
	docker run -ti -p "8080:8080" -p "9099:9099" \
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