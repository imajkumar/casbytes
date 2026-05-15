# CasBytes — Git conventions & branching

## Commit messages (Conventional Commits)

```
<type>(<scope>): <short summary>

Optional body explaining motivation, migration notes, or risk.

Optional footer: BREAKING CHANGE: / Refs: TICKET-123
```

**Types**

- `feat` — new capability
- `fix` — bug fix
- `docs` — documentation only
- `refactor` — behavior-preserving code change
- `test` — tests only
- `chore` — tooling, CI, formatting

**Examples**

```
feat(core): add reference item module template
fix(health): handle skipped redis checks when autoconfig disabled
docs(onboarding): clarify secret handling for local dev
```

## Branch strategy (GitFlow-inspired, simplified)

| Branch | Purpose |
|--------|---------|
| `main` | always releasable; protected |
| `develop` | integration branch (optional if trunk-based) |
| `feature/<ticket>-short-name` | feature work |
| `bugfix/<ticket>-short-name` | non-urgent fixes |
| `hotfix/<ticket>-short-name` | production emergency fixes from `main` |

**Rules**

1. Prefer **short-lived branches** merged via pull requests with review.
2. Rebase or merge according to team policy; never rewrite shared history without agreement.
3. Tag releases from `main` using semantic versions once the platform adopts semver (`v0.3.0`).

## Pull requests

1. Describe **motivation**, **implementation notes**, and **test evidence**.
2. Link tracking tickets (`Refs: CB-123`).
3. Call out database migrations and rollout order explicitly.

## Security

1. Never push credentials to git remotes.
2. If a secret is accidentally committed, **rotate** it immediately and purge history per company incident response policy.
