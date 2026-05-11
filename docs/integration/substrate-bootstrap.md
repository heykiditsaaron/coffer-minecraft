# Substrate Bootstrap

## Purpose

This document records the intended early re-foundation dependency path between
`coffer-minecraft` and the split substrate repositories.

## Default Local Workflow

Default local development should use published snapshot artifacts from:

- `coffer-core`
- `coffer-runtime`
- `coffer-transferable-value-authority`

The initial local resolution mechanism should be `mavenLocal`.

This keeps `coffer-minecraft` consuming artifact boundaries rather than
subproject paths from substrate repositories.

The repository now expects the bootstrap coordinates to be centralized through
Gradle properties:

- `coffer_group`
- `coffer_version`

Subprojects should consume those properties rather than hardcoding literal
substrate versions independently.

## Why `mavenLocal` First

`mavenLocal` is the safest early bootstrap choice because it:

- preserves repository separation
- keeps dependency expectations explicit
- avoids monolithic composite-build assumptions
- reduces accidental coupling to substrate repository internals

## Secondary Workflow

If active cross-repo API work later requires tighter iteration speed, an
explicit split-repository composite-build workflow may be added later.

That workflow should remain optional. It should not become the default bootstrap
assumption for `coffer-minecraft`.

The repository no longer assumes `includeBuild('../coffer')` or substrate
subproject substitution paths from a monolithic checkout.

## Preconditions Before Implementation

Before real Fabric implementation resumes, this repository should have:

- confirmed substrate coordinates
- confirmed snapshot versioning
- one documented default artifact resolution path
- removed monolithic `../coffer` assumptions from Gradle settings
- successful dependency resolution for `bindings/inventory`
- successful dependency resolution for `platforms/fabric`

## Current Bootstrap Boundary

This repository is now prepared for artifact-first bootstrap, but it still
depends on locally published or otherwise resolvable substrate artifacts being
present for compile or test work.

That artifact publication and first compile verification remain separate follow-up
steps.

