# Repository Re-Foundation Structure

## Summary

Prepared the repository structure and operational guidance for the Minecraft
super-platform role without changing implementation behavior.

## Decision

The repository now explicitly documents its Minecraft-specific boundaries,
creates the intended documentation directories, and preserves historical source
as migration/reference material while deleting only clearly generated artifacts.

## Rationale

The repurposing audit established the target shape and cleanup sequence. This
step makes the repository easier to operate safely before any dependency rewiring
or renewed platform implementation begins.

## Scope

Included:

- replacing repository-specific `AGENTS.md` guidance
- updating root boundary guidance in `README.md`
- creating missing structure and lightweight placeholder READMEs
- clarifying repository ownership boundaries
- deleting generated Gradle/build/log artifacts

Excluded:

- dependency rewiring
- substrate integration changes
- source code migration
- Fabric implementation work
- gameplay adapters

## Verification

Run:

```text
git status --short
git diff --check
```

## Uncertainties

- Build wiring still reflects the prior repository era and remains deferred.
- Existing implementation source is preserved for reference until dependency and
  boundary follow-up work is explicitly started.

