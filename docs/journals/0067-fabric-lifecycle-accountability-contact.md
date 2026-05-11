# 0067 Fabric Lifecycle Accountability Contact

## Summary

Introduced the first real platform-contact seam for accountability in
`platforms/fabric`.

The seam is intentionally narrow:

- `SERVER_STARTED`
- `SERVER_STOPPED`

Both now append minimal `SER` lifecycle records to `logs/coffer` without adding
gameplay, exchange orchestration, or wider logging infrastructure.

## Decision

Use bounded Fabric lifecycle participation as the first controlled contact with
platform reality.

Emit only:

- `interactionId`
- `recordType`
- `stage`

for startup/shutdown lifecycle accountability.

Do not add `CER`, timestamps, sequence fields, explanation text, or operational
log management in this step.

## Rationale

This seam introduces real filesystem and real Fabric lifecycle contact while
keeping participation depth deliberately shallow.

That is enough to pressure-test whether the current accountability topology
survives contact with:

- actual Fabric lifecycle callbacks
- actual server run-directory paths
- actual append-only JSONL writes under `logs/coffer`

The current topology survived without structural expansion. Startup and shutdown
remain `SER`-only because no lawful substrate contact or gameplay mutation
participation occurs there.

## Scope

Included:

- lifecycle accountability helper in `platforms/fabric`
- startup append under `logs/coffer/fabric-lifecycle.jsonl`
- shutdown append under the same file
- narrow unit proof for append order, omission, and readability
- entrypoint registration at real Fabric lifecycle callbacks

Excluded:

- gameplay systems
- production adapters
- exchange accountability projection
- timestamps
- operational logging infrastructure
- replay, sequence, or rotation systems

## Verification

Covered by tests and compile validation:

- lifecycle records append under `logs/coffer`
- startup/shutdown lines stay minimal and readable
- omitted fields remain omitted
- Fabric compile still passes with the real lifecycle seam attached

## Uncertainties

Reality introduces a new pressure surface:

- lifecycle accountability failures now need to avoid destabilizing server
  startup/shutdown

This step handles that by containing emission failures to warning logs.

Still intentionally deferred:

- gameplay participation
- live exchange accountability on the real platform path
- operational log management
- any claim of production readiness
