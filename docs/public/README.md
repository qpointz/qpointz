# Mill Documentation

This is a fresh documentation structure for Mill.

## Reference Documentation

The previous documentation is preserved in `../public_old/` for reference. Key files include:

- `src/quickstart/moneta.md` - Complete Moneta quickstart guide
- `src/quickstart/chat-assistant-intents.md` - Chat assistant intent documentation
- `src/index.md` - Original landing page
- `mkdocs.yml` - MkDocs configuration (same structure)

## Current Structure

```
public/
├── mkdocs.yml          # MkDocs configuration
├── requirements.txt    # Python dependencies
├── .gitignore         # Git ignore rules
├── overrides/         # Theme overrides
└── src/               # Documentation source
    ├── index.md       # Landing page
    ├── quickstart.md  # Quickstart guide (to be written)
    └── installation.md # Installation guide (to be written)
```

## Building the Documentation

1. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

2. Serve locally:
   ```bash
   mkdocs serve
   ```

3. Build static site:
   ```bash
   mkdocs build
   ```

## Next Steps

Start by filling in the placeholder pages:
- `src/index.md` - Main landing page
- `src/quickstart.md` - Quickstart guide
- `src/installation.md` - Installation instructions

Reference `../public_old/` for content ideas and structure.
