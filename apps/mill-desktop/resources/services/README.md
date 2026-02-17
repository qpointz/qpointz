Place platform-specific service launch artifacts in this directory before packaging.

Suggested layout:

- `bin/` startup scripts or binaries
- `assets/` service logos
- `config/` optional service config files

Make sure `resources/services.manifest.yaml` points to valid paths relative to `resources/`.
