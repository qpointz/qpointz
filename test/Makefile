QSYNTH_IMAGE ?= qpointz/qsynth
TEST_DIR := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))

.PHONY: help regen-skymill regen-moneta regen-airlines

help:
	@echo "Dataset generation (uses $(QSYNTH_IMAGE)):"
	@echo "  make regen-skymill  # Regenerate skymill dataset (csv, parquet)"
	@echo "  make regen-moneta  # Regenerate moneta dataset"
	@echo "  make regen-airlines # Regenerate airlines dataset"

regen-skymill:
	docker pull $(QSYNTH_IMAGE) && \
	docker run -v "$(TEST_DIR):/data" $(QSYNTH_IMAGE) run --input-file /data/skymill.yaml -a

regen-moneta:
	docker pull $(QSYNTH_IMAGE) && \
	docker run -v "$(TEST_DIR):/data" $(QSYNTH_IMAGE) run --input-file /data/moneta.yaml -a

regen-airlines:
	docker pull $(QSYNTH_IMAGE) && \
	docker run -v "$(TEST_DIR):/data" $(QSYNTH_IMAGE) run --input-file /data/airlines.yaml -a
