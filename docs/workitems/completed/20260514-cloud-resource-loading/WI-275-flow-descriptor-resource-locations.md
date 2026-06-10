# WI-275 — Flow descriptor resource locations

## Goal

Make Spring Boot flow backend source descriptor YAML configurable as resource locations while
leaving direct Calcite/`FlowSchemaFactory` operands file-oriented.

## Problem

Flow backend startup currently assumes descriptor files are local:

- `FlowBackendAutoConfiguration` maps configured `mill.data.backend.flow.sources` values to
  `Path`.
- `SourceDefinitionReader.read(Path)` parses YAML from `path.toFile()`.
- `SingleFileSourceRepository` and `MultiFileSourceRepository` hold `Path` instances.
- `FlowSchemaFactory` accepts only a `descriptorFile` Calcite operand and validates it with
  `java.io.File`.
- `CalciteBackendProperties.model` is documented and wired as a model file path.

That prevents a cloud deployment from storing flow descriptors in object storage even though the
tables referenced by those descriptors can already use cloud blob sources.

## Target Design

Keep the existing property name:

```yaml
mill:
  data:
    backend:
      type: flow
      flow:
        sources:
          - classpath:flow/skymill-flow.yml
          - file:/etc/mill/flow/skymill-flow.yml
          - s3://mill-config-prod/flow/skymill-flow.yml
          - gs://mill-config-prod/flow/skymill-flow.yml
          - azure-blob://config-container/flow/skymill-flow.yml
```

Update flow descriptor loading so `sources` are interpreted as backend resource locations:

- `SourceDefinitionReader` reads from `BackendResourceLoader.open(location)` instead of
  `Path.toFile()`.
- `MultiFileSourceRepository` stores configured location strings, not only `Path`.
- Duplicate source name errors include the resource display location.
- Existing constructors that accept `Path` may remain as compatibility helpers but delegate to the
  resource-location implementation.
- `FlowBackendAutoConfiguration` injects the `BackendResourceLoader` bean, which is backed by Spring
  `ResourceLoader`, and passes raw source locations to the repository.

For Calcite/direct non-Spring usage:

- Keep `FlowSchemaFactory.descriptorFile` as a local-file operand.
- Do not implement `descriptorResource` in this story.
- Do not introduce Spring into `data/mill-data-source-calcite`.
- Do not add a dependency from `data/mill-data-source-calcite` to `data/mill-data-backend-core`.
- Keep `mill.data.backend.calcite.model` file-oriented when Calcite requires a physical file path.
  Document mounted files (ConfigMap, Secret, volume, init container, or equivalent) as the supported
  cloud deployment approach for direct Calcite model use cases.

## Constraints

- `data/mill-data-backends` and `data/mill-data-source-calcite` remain Spring-free.
- No cloud SDK imports in flow backend/core modules.
- Existing local filesystem configurations keep working unchanged.
- Descriptor parsing still uses `SourceObjectMapper.yaml`.
- Descriptor loading errors identify the configured location and root cause.

## Acceptance Criteria

- `mill.data.backend.flow.sources` accepts `classpath:`, `file:`, and bare local paths.
- Flow repositories consume `BackendResourceLoader` or equivalent instead of assuming `Path`.
- Existing tests for local descriptor loading still pass.
- New tests cover classpath descriptor loading and bare path compatibility.
- `FlowSchemaFactory` is explicitly documented as local-file oriented and receives no new
  `descriptorResource` implementation.
- Calcite backend model handling is explicitly documented as requiring a local file if Calcite
  itself requires filesystem access, with mounted files as the supported cloud deployment approach.
- No Spring/cloud SDK dependency is added to clean flow modules.

## Verification

- Run targeted tests for `data/mill-data-backends`, `data/mill-data-autoconfigure`, and
  `data/mill-data-source-calcite`.
- Run at least one flow backend context test with a `classpath:` descriptor location.

## Notes

Cloud-backed flow descriptor tests depend on WI-277 provider registration and may be completed in
WI-278 if emulator setup is too broad for this WI.
