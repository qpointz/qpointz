SHELL := /bin/bash

DESKTOP_DIR := mill-desktop

.PHONY: help build-docker run-moneta run-mill-postgres \
	desktop-install desktop-build desktop-launch desktop-clean \
	jdeps-report jlink-modules

help:
	@echo "Available targets:"
	@echo ""
	@echo "make build-docker      # rebuild docker images"
	@echo "make run-moneta        # runs moneta sample docker"
	@echo "make run-mill-postgres # run mill-service with postgres profile"
	@echo "make jdeps-report      # build edition & generate jdeps/jlink module report"
	@echo "                       #   EDITION=basic (default) | complete | ..."
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
	./gradlew clean installBootDist -Pedition=complete
	$(MAKE) jlink-modules EDITION=complete

build-docker: rebuild
	docker buildx build --tag mill-service \
						--progress plain \
						--file mill-service/src/main/docker/base/Dockerfile \
						--build-arg  APP_EDITION=complete \
						./mill-service
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

EDITION ?= basic
INSTALL_DIR := mill-service/build/install/mill-service-boot-$(EDITION)
JDEPS_DIR   := mill-service/build/jdeps/$(EDITION)
MODULES_DIR := mill-service/build/jlink-modules/$(EDITION)
# jdeps misses JMX remote / -javaagent; must match .gitlab/templates/apps-package-dist.yml
JLINK_RUNTIME_MODULES := jdk.crypto.ec,jdk.crypto.cryptoki,java.naming,jdk.naming.dns,java.sql,java.management,jdk.management.agent,java.instrument

jdeps-report:
	cd ../ && ./gradlew --no-daemon --console plain clean :apps:mill-service:installBootDist -Pedition=$(EDITION)
	$(MAKE) jlink-modules EDITION=$(EDITION)

jlink-modules:
	$(eval FAT_JAR := $(shell find $(INSTALL_DIR)/lib -name 'mill-service-*.jar' ! -name '*-plain.jar' | head -1))
	@if [ -z "$(FAT_JAR)" ]; then echo "ERROR: fat jar not found in $(INSTALL_DIR)/lib"; exit 1; fi
	rm -rf $(JDEPS_DIR) && mkdir -p $(JDEPS_DIR) $(MODULES_DIR)
	cd $(JDEPS_DIR) && jar xf $(CURDIR)/$(FAT_JAR) BOOT-INF/classes BOOT-INF/lib
	$$JAVA_HOME/bin/jdeps \
		--multi-release 25 \
		--ignore-missing-deps \
		--recursive \
		--print-module-deps \
		--class-path "$(CURDIR)/$(JDEPS_DIR)/BOOT-INF/lib/*" \
		"$(CURDIR)/$(JDEPS_DIR)/BOOT-INF/classes" 2>/dev/null \
		| { grep -E '^[a-z]' || true; } > $(MODULES_DIR)/jdeps-modules.csv
	@if [ ! -s "$(MODULES_DIR)/jdeps-modules.csv" ]; then echo "ERROR: jdeps produced no module dependencies for $(EDITION)"; exit 1; fi
	@echo "$$(cat $(MODULES_DIR)/jdeps-modules.csv),$(JLINK_RUNTIME_MODULES)" > $(MODULES_DIR)/jdeps-modules.csv
	tr ',' '\n' < $(MODULES_DIR)/jdeps-modules.csv | sed '/^$$/d' | sort -u > $(MODULES_DIR)/jdeps-modules.txt
	$$JAVA_HOME/bin/java --list-modules | cut -d@ -f1 | sort -u > $(MODULES_DIR)/jdk-modules.txt
	comm -12 $(MODULES_DIR)/jdeps-modules.txt $(MODULES_DIR)/jdk-modules.txt | paste -sd, - > $(MODULES_DIR)/jlink-modules.txt
	@echo ""
	@echo "=== jdeps modules ($(EDITION)) ==="
	@cat $(MODULES_DIR)/jdeps-modules.csv
	@echo ""
	@echo ""
	@echo "=== jlink-eligible modules ==="
	@cat $(MODULES_DIR)/jlink-modules.txt
	@echo ""
	@echo ""
	@echo "Reports written to $(MODULES_DIR)/"

run-mill-postgres:
	cd ../ && \
	MILL_DB_URL=$${MILL_DB_URL:-jdbc:postgresql://localhost:5432/mill} \
	MILL_DB_USERNAME=$${MILL_DB_USERNAME:-postgres} \
	MILL_DB_PASSWORD=$${MILL_DB_PASSWORD:-postgres} \
	./gradlew :apps:mill-service:bootRun --args='--spring.profiles.active=skymill,postgres'

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