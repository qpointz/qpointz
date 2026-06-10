VENV_DIR := .venv
PIP := $(VENV_DIR)/bin/pip
MKDOCS := $(VENV_DIR)/bin/mkdocs
PYTHON := $(VENV_DIR)/bin/python
DOCS_DIR := public

.PHONY: help install serve build clean

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-12s %s\n", $$1, $$2}'

$(VENV_DIR):
	python3 -m venv $(VENV_DIR)

install: $(VENV_DIR) ## Install dependencies into local venv
	$(PIP) install --upgrade pip
	$(PIP) install -r $(DOCS_DIR)/requirements.txt

serve: ## Run mkdocs dev server (http://localhost:8000)
	cd $(DOCS_DIR) && ../$(MKDOCS) serve

build: ## Build static site into public/site/
	cd $(DOCS_DIR) && ../$(MKDOCS) build

clean: ## Remove venv and build artifacts
	rm -rf $(VENV_DIR) $(DOCS_DIR)/site
