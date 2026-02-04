# DataChat

A modern AI chat interface with integrated data model exploration and business context browsing.

## Features

- **Chat** - AI chat with markdown support, code highlighting, conversation history
- **Data Model** - Browse schemas, tables, columns with metadata facets
- **Context** - Explore business concepts by category and tag

## Tech Stack

- React 19 + TypeScript + Vite
- Mantine UI v8
- React Router v7
- react-markdown + shiki

## Quick Start

```bash
# Install dependencies
npm install

# Start dev server
npm run dev

# Open http://localhost:5173
```

## Project Structure

```
src/
├── components/
│   ├── layout/      # AppHeader, AppShell, Sidebar
│   ├── chat/        # Chat components
│   ├── data-model/  # Schema browser
│   └── context/     # Concept explorer
├── data/            # Mock data
├── types/           # TypeScript interfaces
├── theme/           # Mantine theme
└── context/         # React Context providers
```

## Design

- **Light mode**: Teal primary (#0d9488)
- **Dark mode**: Cyan accent (#22d3ee)
- **Neutral**: Slate palette

## Documentation

See [ARCHITECTURE.md](./ARCHITECTURE.md) for detailed documentation.

## Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start dev server |
| `npm run build` | Production build |
| `npm run preview` | Preview build |
| `npm run test` | Run tests |
| `npm run lint` | Lint code |
