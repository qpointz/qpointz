# Security Design

This folder contains security architecture and implementation design notes for:

- authentication and identity
- authorization and policy integration
- token models (including PAT)
- security operational modes (enabled/disabled)

## Documents

| File | Purpose |
|------|---------|
| `auth-profile-pat-architecture.md` | Proposed architecture for forms login, persistent user profiles, PAT issuance/validation, and OAuth-ready identity persistence with support for disabling security |
| `user-identity-jpa-implementation.md` | Detailed implementation design covering the full vertical slice: database schema, JPA entities, repositories, service layer, auth REST API, audit trail, autoconfiguration, and feature flags |
| `user-profile-extensibility.md` | How to extend the user profile beyond the WI-088 baseline — new columns vs domain extension tables, decision guide |

